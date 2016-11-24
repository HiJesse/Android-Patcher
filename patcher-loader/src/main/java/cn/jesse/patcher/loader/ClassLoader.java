package cn.jesse.patcher.loader;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipFile;

import cn.jesse.patcher.loader.util.PatchFileUtil;
import cn.jesse.patcher.loader.util.ReflectUtil;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

/**
 * Created by jesse on 21/11/2016.
 */
public class ClassLoader {
    private static final String TAG = Constants.LOADER_TAG + "ClassLoaderAdd";

    private static final String CHECK_DEX_CLASS = "cn.jesse.patcher.loader.PatcherTestDexLoad";
    private static final String CHECK_DEX_FIELD = "isPatch";

    private static int sPatchDexCount = 0;

    @SuppressLint("NewApi")
    public static void installDexes(Application application, PathClassLoader loader, File dexOptDir, List<File> files)
            throws Throwable {

        if (!files.isEmpty()) {
            java.lang.ClassLoader classLoader = loader;
            if (Build.VERSION.SDK_INT >= 24) {
                classLoader = AndroidNClassLoader.inject(loader, application);
            }
            //because in dalvik, if inner class is not the same classloader with it wrapper class.
            //it won't fail at dex2opt
            if (Build.VERSION.SDK_INT >= 23) {
                V23.install(classLoader, files, dexOptDir);
            } else if (Build.VERSION.SDK_INT >= 19) {
                V19.install(classLoader, files, dexOptDir);
            } else if (Build.VERSION.SDK_INT >= 14) {
                V14.install(classLoader, files, dexOptDir);
            } else {
                V4.install(classLoader, files, dexOptDir);
            }
            //install done
            sPatchDexCount = files.size();

            if (!checkDexInstall(classLoader)) {
                //reset patch dex
                ClassLoader.uninstallPatchDex(classLoader);
                throw new PatcherRuntimeException(Constants.CHECK_DEX_INSTALL_FAIL);
            }
        }
    }

    public static void uninstallPatchDex(java.lang.ClassLoader classLoader) throws Throwable {
        if (sPatchDexCount <= 0) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 14) {
            Field pathListField = ReflectUtil.findField(classLoader, "pathList");
            Object dexPathList = pathListField.get(classLoader);
            ReflectUtil.reduceFieldArray(dexPathList, "dexElements", sPatchDexCount);
        } else {
            ReflectUtil.reduceFieldArray(classLoader, "mPaths", sPatchDexCount);
            ReflectUtil.reduceFieldArray(classLoader, "mFiles", sPatchDexCount);
            ReflectUtil.reduceFieldArray(classLoader, "mZips", sPatchDexCount);
            try {
                ReflectUtil.reduceFieldArray(classLoader, "mDexs", sPatchDexCount);
            } catch (Exception e) {
            }
        }
    }

    private static boolean checkDexInstall(java.lang.ClassLoader classLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = Class.forName(CHECK_DEX_CLASS, true, classLoader);
        Field filed = ReflectUtil.findField(clazz, CHECK_DEX_FIELD);
        boolean isPatch = (boolean) filed.get(null);
        Log.w(TAG, "checkDexInstall result:" + isPatch);
        return isPatch;
    }

    /**
     * Installer for platform versions 19.
     */
    private static final class V23 {

        private static void install(java.lang.ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IOException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = ReflectUtil.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
            ReflectUtil.expandFieldArray(dexPathList, "dexElements", makePathElements(dexPathList,
                    new ArrayList<File>(additionalClassPathEntries), optimizedDirectory,
                    suppressedExceptions));
            if (suppressedExceptions.size() > 0) {
                for (IOException e : suppressedExceptions) {
                    Log.w(TAG, "Exception in makePathElement", e);
                    throw e;
                }

            }
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makePathElements}.
         */
        private static Object[] makePathElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory,
                ArrayList<IOException> suppressedExceptions)
                throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

            Method makePathElements;
            try {
                makePathElements = ReflectUtil.findMethod(dexPathList, "makePathElements", List.class, File.class,
                        List.class);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NoSuchMethodException: makePathElements(List,File,List) failure");
                try {
                    makePathElements = ReflectUtil.findMethod(dexPathList, "makePathElements", ArrayList.class, File.class, ArrayList.class);
                } catch (NoSuchMethodException e1) {
                    Log.e(TAG, "NoSuchMethodException: makeDexElements(ArrayList,File,ArrayList) failure");
                    try {
                        Log.e(TAG, "NoSuchMethodException: try use v19 instead");
                        return V19.makeDexElements(dexPathList, files, optimizedDirectory, suppressedExceptions);
                    } catch (NoSuchMethodException e2) {
                        Log.e(TAG, "NoSuchMethodException: makeDexElements(List,File,List) failure");
                        throw e2;
                    }
                }
            }

            return (Object[]) makePathElements.invoke(dexPathList, files, optimizedDirectory, suppressedExceptions);
        }
    }

    /**
     * Installer for platform versions 19.
     */
    private static final class V19 {

        private static void install(java.lang.ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IOException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = ReflectUtil.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
            ReflectUtil.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                    new ArrayList<File>(additionalClassPathEntries), optimizedDirectory,
                    suppressedExceptions));
            if (suppressedExceptions.size() > 0) {
                for (IOException e : suppressedExceptions) {
                    Log.w(TAG, "Exception in makeDexElement", e);
                    throw e;
                }
            }
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory,
                ArrayList<IOException> suppressedExceptions)
                throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

            Method makeDexElements = null;
            try {
                makeDexElements = ReflectUtil.findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class,
                        ArrayList.class);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NoSuchMethodException: makeDexElements(ArrayList,File,ArrayList) failure");
                try {
                    //Android 4.4 5.0 5.1 参数都是ArrayList,但是Tinker在线上有碰到机子方法参数是List
                    makeDexElements = ReflectUtil.findMethod(dexPathList, "makeDexElements", List.class, File.class, List.class);
                } catch (NoSuchMethodException e1) {
                    Log.e(TAG, "NoSuchMethodException: makeDexElements(List,File,List) failure");
                    throw e1;
                }
            }

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory, suppressedExceptions);
        }
    }

    /**
     * Installer for platform versions 14, 15, 16, 17 and 18.
     */
    private static final class V14 {

        private static void install(java.lang.ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            //反射得到PathClassLoader中的pathList对象.
            Field pathListField = ReflectUtil.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            //通过反射调用makeDexElements方法生成补丁包的dex数组,再将其插入到dexElements的头部
            ReflectUtil.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                    new ArrayList<File>(additionalClassPathEntries), optimizedDirectory));
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory)
                throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException {
            Method makeDexElements =
                    ReflectUtil.findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class);

            //反射调用makeDexElements方法根据files得到新dexElements数组
            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory);
        }
    }

    /**
     * Installer for platform versions 4 to 13.
     */
    private static final class V4 {
        private static void install(java.lang.ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, IOException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.DexClassLoader. We modify its
             * fields mPaths, mFiles, mZips and mDexs to append additional DEX
             * file entries.
             */
            int extraSize = additionalClassPathEntries.size();

            Field pathField = ReflectUtil.findField(loader, "path");

            //反射拿到path属性
            StringBuilder path = new StringBuilder((String) pathField.get(loader));
            //根据补丁文件的个数建立四个关键属性对应的数组
            String[] extraPaths = new String[extraSize];
            File[] extraFiles = new File[extraSize];
            ZipFile[] extraZips = new ZipFile[extraSize];
            DexFile[] extraDexs = new DexFile[extraSize];
            //通过遍历补丁文件,对四个数组进行填充
            for (ListIterator<File> iterator = additionalClassPathEntries.listIterator();
                 iterator.hasNext();) {
                File additionalEntry = iterator.next();
                String entryPath = additionalEntry.getAbsolutePath();
                path.append(':').append(entryPath);
                int index = iterator.previousIndex();
                extraPaths[index] = entryPath;
                extraFiles[index] = additionalEntry;
                extraZips[index] = new ZipFile(additionalEntry);
                //edit by zhangshaowen
                String outputPathName = PatchFileUtil.optimizedPathFor(additionalEntry, optimizedDirectory);
                //for below 4.0, we must input jar or zip
                extraDexs[index] = DexFile.loadDex(entryPath, outputPathName, 0);
            }

            //将path属性反射赋值
            pathField.set(loader, path.toString());
            //将新的数组插入到原数组的前部,完成补丁加载的动作
            ReflectUtil.expandFieldArray(loader, "mPaths", extraPaths);
            ReflectUtil.expandFieldArray(loader, "mFiles", extraFiles);
            ReflectUtil.expandFieldArray(loader, "mZips", extraZips);
            try {
                ReflectUtil.expandFieldArray(loader, "mDexs", extraDexs);
            } catch (Exception e) {

            }
        }
    }
}

package cn.jesse.patcher.loader;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.jesse.patcher.loader.util.ReflectUtil;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

/**
 * Created by jesse on 21/11/2016.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class AndroidNClassLoader extends PathClassLoader {
    static ArrayList<DexFile> oldDexFiles = new ArrayList<>();
    PathClassLoader originClassLoader;

    private AndroidNClassLoader(String dexPath, PathClassLoader parent) {
        super(dexPath, parent.getParent());
        originClassLoader = parent;
    }

    private static AndroidNClassLoader createAndroidNClassLoader(PathClassLoader original) throws Exception {
        //根据原PathClassLoader的parent 构建出AndroidNClassLoader
        AndroidNClassLoader androidNClassLoader = new AndroidNClassLoader("",  original);
        //反射拿到original的pathList
        Field originPathList = ReflectUtil.findField(original, "pathList");
        Object originPathListObject = originPathList.get(original);

        //反射拿到pathList对象的definingContext属性,因为该属性是original的引用,需要拿到之后替换成新loader的引用
        Field originClassloader = ReflectUtil.findField(originPathListObject, "definingContext");
        originClassloader.set(originPathListObject, androidNClassLoader);

        //反射拿到androidNClassLoader的pathList对象,并且替换成original的
        Field pathListField = ReflectUtil.findField(androidNClassLoader, "pathList");
        //just use PathClassloader's pathList
        pathListField.set(androidNClassLoader, originPathListObject);

        //we must recreate dexFile due to dexCache
        //反射拿到original的pathList的dexElements
        List<File> additionalClassPathEntries = new ArrayList<>();
        Field dexElement = ReflectUtil.findField(originPathListObject, "dexElements");
        Object[] originDexElements = (Object[]) dexElement.get(originPathListObject);

        //遍历出dexElements中真实的dex文件名之后存储起来.
        for (Object element : originDexElements) {
            DexFile dexFile = (DexFile) ReflectUtil.findField(element, "dexFile").get(element);
            additionalClassPathEntries.add(new File(dexFile.getName()));
            //protect for java.lang.AssertionError: Failed to close dex file in finalizer.
            oldDexFiles.add(dexFile);
        }

        //反射拿到original的pathList的makePathElements方法
        Method makePathElements = ReflectUtil.findMethod(originPathListObject, "makePathElements", List.class, File.class,
                List.class);
        ArrayList<IOException> suppressedExceptions = new ArrayList<>();
        //利用makePathElements方法重新生成dexElements数组,并替换原来的数组
        //最重要的是null参数,看Android7.0的源码不传递opt路径的话会重新load dex文件,从而达到修复混编时热修复的问题.
        Object[] newDexElements = (Object[]) makePathElements.invoke(originPathListObject, additionalClassPathEntries, null, suppressedExceptions);
        dexElement.set(originPathListObject, newDexElements);
        return androidNClassLoader;
    }

    private static void reflectPackageInfoClassloader(Application application, ClassLoader reflectClassLoader) throws Exception {
        String defBase = "mBase";
        String defPackageInfo = "mPackageInfo";
        String defClassLoader = "mClassLoader";

        //替换application持有的Context对象中 LoadedApk对象的属性classloader 为AndroidNClassLoader
        Context baseContext = (Context) ReflectUtil.findField(application, defBase).get(application);
        Object basePackageInfo = ReflectUtil.findField(baseContext, defPackageInfo).get(baseContext);
        Field classLoaderField = ReflectUtil.findField(basePackageInfo, defClassLoader);
        //设置Thread中持有的ClassLoader为AndroidNClassLoader
        Thread.currentThread().setContextClassLoader(reflectClassLoader);
        classLoaderField.set(basePackageInfo, reflectClassLoader);
    }

    public static AndroidNClassLoader inject(PathClassLoader originClassLoader, Application application) throws Exception {
        AndroidNClassLoader classLoader = createAndroidNClassLoader(originClassLoader);
        reflectPackageInfoClassloader(application, classLoader);
        return classLoader;
    }

//    public static String getLdLibraryPath(DexLoader loader) throws Exception {
//        String nativeLibraryPath;
//
//        nativeLibraryPath = (String) loader.getClass()
//            .getMethod("getLdLibraryPath", new Class[0])
//            .invoke(loader, new Object[0]);
//
//        return nativeLibraryPath;
//    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    public String findLibrary(String name) {
        return super.findLibrary(name);
    }
}
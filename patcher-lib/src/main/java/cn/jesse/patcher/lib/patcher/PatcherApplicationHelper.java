package cn.jesse.patcher.lib.patcher;

import android.content.Intent;

import java.io.File;
import java.util.HashMap;

import cn.jesse.patcher.lib.util.PatcherLog;
import cn.jesse.patcher.loader.Constants;
import cn.jesse.patcher.loader.PatcherRuntimeException;
import cn.jesse.patcher.loader.app.AbsApplicationProxy;
import cn.jesse.patcher.loader.util.IntentUtil;
import cn.jesse.patcher.loader.util.PatchFileUtil;
import cn.jesse.patcher.loader.util.PatcherInternals;

/**
 * Created by jesse on 04/12/2016.
 */

public class PatcherApplicationHelper {
    private static final String TAG = Constants.LOADER_TAG + "PatcherApplicationHelper";

    /**
     * they can use without Patcher is installed!
     * same as {@code Patcher.isPatcherEnabled}
     *
     * @return
     */
    public static boolean isPatcherEnableAll(AbsApplicationProxy applicationProxy) {
        if (applicationProxy == null || applicationProxy.getApplication() == null) {
            throw new PatcherRuntimeException("patcherApplication is null");
        }
        int patcherFlags = applicationProxy.getPatchFlags();
        return PatcherInternals.isPatcherEnabledAll(patcherFlags);
    }

    /**
     * same as {@code Patcher.isEnabledForDex}
     *
     * @param applicationProxy
     * @return
     */
    public static boolean isPatcherEnableForDex(AbsApplicationProxy applicationProxy) {
        if (applicationProxy == null || applicationProxy.getApplication() == null) {
            throw new PatcherRuntimeException("patcherApplication is null");
        }
        int patcherFlags = applicationProxy.getPatchFlags();
        return PatcherInternals.isPatcherEnabledForDex(patcherFlags);
    }

    /**
     * same as {@code Patcher.isEnabledForNativeLib}
     *
     * @param applicationProxy
     * @return
     */
    public static boolean isPatcherEnableForNativeLib(AbsApplicationProxy applicationProxy) {
        if (applicationProxy == null || applicationProxy.getApplication() == null) {
            throw new PatcherRuntimeException("patcherApplication is null");
        }
        int patcherFlags = applicationProxy.getPatchFlags();
        return PatcherInternals.isPatcherEnabledForNativeLib(patcherFlags);
    }

    /**
     * same as {@code Patcher.isPatcherEnabledForResource}
     *
     * @param applicationProxy
     * @return
     */
    public static boolean isPatcherEnableForResource(AbsApplicationProxy applicationProxy) {
        if (applicationProxy == null || applicationProxy.getApplication() == null) {
            throw new PatcherRuntimeException("patcherApplication is null");
        }
        int patcherFlags = applicationProxy.getPatchFlags();
        return PatcherInternals.isPatcherEnabledForResource(patcherFlags);
    }

    /**
     * same as {@code Patcher.getPatchDirectory}
     *
     * @param applicationProxy
     * @return
     */
    public static File getPatcherPatchDirectory(AbsApplicationProxy applicationProxy) {
        if (applicationProxy == null || applicationProxy.getApplication() == null) {
            throw new PatcherRuntimeException("patcherApplication is null");
        }

        return PatchFileUtil.getPatchDirectory(applicationProxy.getApplication());
    }

    /**
     * whether patcher is success loaded
     * same as {@code Patcher.isPatcherLoaded}
     *
     * @param applicationProxy
     * @return
     */
    public static boolean isPatcherLoadSuccess(AbsApplicationProxy applicationProxy) {
        if (applicationProxy == null || applicationProxy.getApplication() == null) {
            throw new PatcherRuntimeException("patcherApplication is null");
        }

        Intent patcherResultIntent = applicationProxy.getPatcherResultIntent();

        if (patcherResultIntent == null) {
            return false;
        }
        int loadCode = IntentUtil.getIntentReturnCode(patcherResultIntent);

        return (loadCode == Constants.ERROR_LOAD_OK);
    }

    /**
     * you can use this api to get load dexes before patcher is installed
     * same as {@code Patcher.getPatcherLoadResultIfPresent.dexes}
     *
     * @return
     */
    public static HashMap<String, String> getLoadDexesAndMd5(AbsApplicationProxy applicationProxy) {
        if (applicationProxy == null || applicationProxy.getApplication() == null) {
            throw new PatcherRuntimeException("patcherApplication is null");
        }

        Intent patcherResultIntent = applicationProxy.getPatcherResultIntent();

        if (patcherResultIntent == null) {
            return null;
        }
        int loadCode = IntentUtil.getIntentReturnCode(patcherResultIntent);

        if (loadCode == Constants.ERROR_LOAD_OK) {
            return IntentUtil.getIntentPatchDexPaths(patcherResultIntent);
        }
        return null;
    }


    /**
     * you can use this api to get load libs before patcher is installed
     * same as {@code Patcher.getPatcherLoadResultIfPresent.libs}
     *
     * @return
     */
    public static HashMap<String, String> getLoadLibraryAndMd5(AbsApplicationProxy applicationProxy) {
        if (applicationProxy == null || applicationProxy.getApplication() == null) {
            throw new PatcherRuntimeException("patcherApplication is null");
        }

        Intent patcherResultIntent = applicationProxy.getPatcherResultIntent();

        if (patcherResultIntent == null) {
            return null;
        }
        int loadCode = IntentUtil.getIntentReturnCode(patcherResultIntent);

        if (loadCode == Constants.ERROR_LOAD_OK) {
            return IntentUtil.getIntentPatchLibsPaths(patcherResultIntent);
        }
        return null;
    }

    /**
     * you can use this api to get patcher package configs before patcher is installed
     * same as {@code Patcher.getPatcherLoadResultIfPresent.packageConfig}
     *
     * @return
     */
    public static HashMap<String, String> getPackageConfigs(AbsApplicationProxy applicationProxy) {
        if (applicationProxy == null || applicationProxy.getApplication() == null) {
            throw new PatcherRuntimeException("patcherApplication is null");
        }

        Intent patcherResultIntent = applicationProxy.getPatcherResultIntent();

        if (patcherResultIntent == null) {
            return null;
        }
        int loadCode = IntentUtil.getIntentReturnCode(patcherResultIntent);

        if (loadCode == Constants.ERROR_LOAD_OK) {
            return IntentUtil.getIntentPackageConfig(patcherResultIntent);
        }
        return null;
    }

    /**
     * you can use this api to get patcher current version before patcher is installed
     *
     * @return
     */
    public static String getCurrentVersion(AbsApplicationProxy applicationProxy) {
        if (applicationProxy == null || applicationProxy.getApplication() == null) {
            throw new PatcherRuntimeException("patcherApplication is null");
        }

        Intent patcherResultIntent = applicationProxy.getPatcherResultIntent();

        if (patcherResultIntent == null) {
            return null;
        }
        final String oldVersion = IntentUtil.getStringExtra(patcherResultIntent, IntentUtil.INTENT_PATCH_OLD_VERSION);
        final String newVersion = IntentUtil.getStringExtra(patcherResultIntent, IntentUtil.INTENT_PATCH_NEW_VERSION);
        final boolean isMainProcess = PatcherInternals.isInMainProcess(applicationProxy.getApplication());
        if (oldVersion != null && newVersion != null) {
            if (isMainProcess) {
                return newVersion;
            } else {
                return oldVersion;
            }
        }
        return null;
    }

    /**
     * clean all patch files without install patcher
     * same as {@code Patcher.cleanPatch}
     *
     * @param applicationProxy
     */
    public static void cleanPatch(AbsApplicationProxy applicationProxy) {
        if (applicationProxy == null || applicationProxy.getApplication() == null) {
            throw new PatcherRuntimeException("patcherApplication is null");
        }
        if (PatcherApplicationHelper.isPatcherLoadSuccess(applicationProxy)) {
            PatcherLog.e(TAG, "it is not safety to clean patch when patcher is loaded, you should kill all your process after clean!");
        }
        PatchFileUtil.deleteDir(PatchFileUtil.getPatchDirectory(applicationProxy.getApplication()));
    }

    /**
     * only support auto load lib/armeabi-v7a library from patch.
     * in some process, you may not want to install patcher
     * and you can load patch dex and library without install patcher!
     * }
     */
    public static void loadArmV7aLibrary(AbsApplicationProxy applicationProxy, String libName) {
        if (libName == null || libName.isEmpty() || applicationProxy == null) {
            throw new PatcherRuntimeException("libName or context is null!");
        }

        if (PatcherApplicationHelper.isPatcherEnableForNativeLib(applicationProxy)) {
            if (PatcherApplicationHelper.loadLibraryFromPatcher(applicationProxy, "lib/armeabi-v7a", libName)) {
                return;
            }

        }
        System.loadLibrary(libName);
    }


    /**
     * only support auto load lib/armeabi library from patch.
     * in some process, you may not want to install patcher
     * and you can load patch dex and library without install patcher!
     */
    public static void loadArmLibrary(AbsApplicationProxy applicationProxy, String libName) {
        if (libName == null || libName.isEmpty() || applicationProxy == null) {
            throw new PatcherRuntimeException("libName or context is null!");
        }

        if (PatcherApplicationHelper.isPatcherEnableForNativeLib(applicationProxy)) {
            if (PatcherApplicationHelper.loadLibraryFromPatcher(applicationProxy, "lib/armeabi", libName)) {
                return;
            }

        }
        System.loadLibrary(libName);
    }

    /**
     * you can use these api to load patcher library without patcher is installed!
     * same as {@code PatcherInstaller#loadLibraryFromPatcher}
     *
     * @param applicationProxy
     * @param relativePath
     * @param libname
     * @return
     * @throws UnsatisfiedLinkError
     */
    public static boolean loadLibraryFromPatcher(AbsApplicationProxy applicationProxy, String relativePath, String libname) throws UnsatisfiedLinkError {
        libname = libname.startsWith("lib") ? libname : "lib" + libname;
        libname = libname.endsWith(".so") ? libname : libname + ".so";
        String relativeLibPath = relativePath + "/" + libname;

        //TODO we should add cpu abi, and the real path later
        if (PatcherApplicationHelper.isPatcherEnableForNativeLib(applicationProxy)
                && PatcherApplicationHelper.isPatcherLoadSuccess(applicationProxy)) {
            HashMap<String, String> loadLibraries = PatcherApplicationHelper.getLoadLibraryAndMd5(applicationProxy);
            if (loadLibraries != null) {
                String currentVersion = PatcherApplicationHelper.getCurrentVersion(applicationProxy);
                if (PatcherInternals.isNullOrNil(currentVersion)) {
                    return false;
                }
                File patchDirectory = PatchFileUtil.getPatchDirectory(applicationProxy.getApplication());
                if (patchDirectory == null) {
                    return false;
                }
                File patchVersionDirectory = new File(patchDirectory.getAbsolutePath() + "/" + PatchFileUtil.getPatchVersionDirectory(currentVersion));
                String libPrePath = patchVersionDirectory.getAbsolutePath() + "/" + Constants.SO_PATH;

                for (String name : loadLibraries.keySet()) {
                    if (name.equals(relativeLibPath)) {
                        String patchLibraryPath = libPrePath + "/" + name;
                        File library = new File(patchLibraryPath);
                        if (library.exists()) {
                            //whether we check md5 when load
                            boolean verifyMd5 = applicationProxy.getPatchLoadVerifyFlag();
                            if (verifyMd5 && !PatchFileUtil.verifyFileMd5(library, loadLibraries.get(name))) {
                                //do not report, because patcher is not install
                                PatcherLog.i(TAG, "loadLibraryFromPatcher md5mismatch fail:" + patchLibraryPath);
                            } else {
                                System.load(patchLibraryPath);
                                PatcherLog.i(TAG, "loadLibraryFromPatcher success:" + patchLibraryPath);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}

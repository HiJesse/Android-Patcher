package cn.jesse.patcher.loader;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cn.jesse.patcher.loader.util.DexDiffPatchInfo;
import cn.jesse.patcher.loader.util.IntentUtil;
import cn.jesse.patcher.loader.util.PatchFileUtil;
import cn.jesse.patcher.loader.util.PatcherInternals;
import cn.jesse.patcher.loader.util.SecurityCheck;
import dalvik.system.PathClassLoader;

/**
 * Created by jesse on 17/11/2016.
 */
public class PatcherDexLoader {
    private static final String TAG = Constants.LOADER_TAG + "PatchDexLoader";

    private static final String                           DEX_MEAT_FILE     = Constants.DEX_META_FILE;
    private static final String                           DEX_PATH          = Constants.DEX_PATH;
    private static final String                           DEX_OPTIMIZE_PATH = Constants.DEX_OPTIMIZE_PATH;
    private static final ArrayList<DexDiffPatchInfo>      dexList           = new ArrayList<>();

    private static boolean   parallelOTAResult;
    private static Throwable parallelOTAThrowable;

    private PatcherDexLoader() {
    }

    public static boolean loadPatcherJars(Application application, boolean tinkerLoadVerifyFlag, String directory, Intent intentResult, boolean isSystemOTA) {
        //先判断checkComplete时过滤出来物理存在的dexList是否为空
        if (dexList.isEmpty()) {
            Log.w(TAG, "there is no dex to load");
            return true;
        }

        //
        PathClassLoader classLoader = (PathClassLoader) PatcherDexLoader.class.getClassLoader();
        if (classLoader != null) {
            Log.i(TAG, "classloader: " + classLoader.toString());
        } else {
            Log.e(TAG, "classloader is null");
            IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_VERSION_DEX_CLASSLOADER_NULL);
            return false;
        }

        String dexPath = directory + "/" + DEX_PATH + "/";
        File optimizeDir = new File(directory + "/" + DEX_OPTIMIZE_PATH);
//        Log.i(TAG, "loadTinkerJars: dex path: " + dexPath);
//        Log.i(TAG, "loadTinkerJars: opt path: " + optimizeDir.getAbsolutePath());

        ArrayList<File> legalFiles = new ArrayList<>();

        final boolean isArtPlatForm = PatcherInternals.isVmArt();
        for (DexDiffPatchInfo info : dexList) {
            //for dalvik, ignore art support dex
            if (isJustArtSupportDex(info)) {
                continue;
            }
            String path = dexPath + info.realName;
            File file = new File(path);

            //如果在Application处配置了tinkerLoadVerifyFlag为true, 则每次加载dex补丁之前都对文件做MD5校验.
            if (tinkerLoadVerifyFlag) {
                long start = System.currentTimeMillis();
                String checkMd5 = isArtPlatForm ? info.destMd5InArt : info.destMd5InDvm;
                if (!PatchFileUtil.verifyDexFileMd5(file, checkMd5)) {
                    //it is good to delete the mismatch file
                    IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_VERSION_DEX_MD5_MISMATCH);
                    intentResult.putExtra(IntentUtil.INTENT_PATCH_MISMATCH_DEX_PATH,
                            file.getAbsolutePath());
                    return false;
                }
                Log.i(TAG, "verify dex file:" + file.getPath() + " md5, use time: " + (System.currentTimeMillis() - start));
            }
            legalFiles.add(file);
        }

        if (isSystemOTA) {
            parallelOTAResult = true;
            parallelOTAThrowable = null;
            Log.w(TAG, "systemOTA, try parallel oat dexes!!!!!");

            ParallelDexOptimizer.optimizeAll(
                    legalFiles, optimizeDir,
                    new ParallelDexOptimizer.ResultCallback() {
                        @Override
                        public void onSuccess(File dexFile, File optimizedDir) {
                            // Do nothing.
                        }
                        @Override
                        public void onFailed(File dexFile, File optimizedDir, Throwable thr) {
                            parallelOTAResult = false;
                            parallelOTAThrowable = thr;
                        }
                    }
            );
            if (!parallelOTAResult) {
                Log.e(TAG, "parallel oat dexes failed");
                intentResult.putExtra(IntentUtil.INTENT_PATCH_EXCEPTION, parallelOTAThrowable);
                IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_VERSION_PARALLEL_DEX_OPT_EXCEPTION);
                return false;
            }
        }

        return false;
    }

    /**
     * all the dex files in meta file exist?
     * fast check, only check whether exist
     *
     * @param directory
     * @return boolean
     */
    public static boolean checkComplete(String directory, SecurityCheck securityCheck, Intent intentResult) {
        String meta = securityCheck.getMetaContentMap().get(DEX_MEAT_FILE);
        //not found dex
        if (meta == null) {
            return true;
        }
        dexList.clear();
        DexDiffPatchInfo.parseDexDiffPatchInfo(meta, dexList);

        if (dexList.isEmpty()) {
            return true;
        }

        HashMap<String, String> dexes = new HashMap<>();

        for (DexDiffPatchInfo info : dexList) {
            //for dalvik, ignore art support dex
            if (isJustArtSupportDex(info)) {
                continue;
            }
            if (!DexDiffPatchInfo.checkDexDiffPatchInfo(info)) {
                intentResult.putExtra(IntentUtil.INTENT_PATCH_PACKAGE_PATCH_CHECK, Constants.ERROR_PACKAGE_CHECK_DEX_META_CORRUPTED);
                IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_PACKAGE_CHECK_FAIL);
                return false;
            }
            dexes.put(info.realName, info.destMd5InDvm);
        }

        //tinker/patch.info/patch-641e634c/dex
        String dexDirectory = directory + "/" + DEX_PATH + "/";

        File dexDir = new File(dexDirectory);

        if (!dexDir.exists() || !dexDir.isDirectory()) {
            IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_VERSION_DEX_DIRECTORY_NOT_EXIST);
            return false;
        }

        String optimizeDexDirectory = directory + "/" + DEX_OPTIMIZE_PATH + "/";
        File optimizeDexDirectoryFile = new File(optimizeDexDirectory);

        //fast check whether there is any dex files missing
        for (String name : dexes.keySet()) {
            File dexFile = new File(dexDirectory + name);
            if (!dexFile.exists()) {
                intentResult.putExtra(IntentUtil.INTENT_PATCH_MISSING_DEX_PATH, dexFile.getAbsolutePath());
                IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_VERSION_DEX_FILE_NOT_EXIST);
                return false;
            }
            //check dex opt whether complete also
            File dexOptFile = new File(PatchFileUtil.optimizedPathFor(dexFile, optimizeDexDirectoryFile));
            if (!dexOptFile.exists()) {
                intentResult.putExtra(IntentUtil.INTENT_PATCH_MISSING_DEX_PATH, dexOptFile.getAbsolutePath());
                IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_VERSION_DEX_OPT_FILE_NOT_EXIST);
                return false;
            }
        }

        //if is ok, add to result intent
        intentResult.putExtra(IntentUtil.INTENT_PATCH_DEXES_PATH, dexes);
        return true;
    }

    private static boolean isJustArtSupportDex(DexDiffPatchInfo dexDiffPatchInfo) {
        if (PatcherInternals.isVmArt()) {
            return false;
        }

        String destMd5InDvm = dexDiffPatchInfo.destMd5InDvm;

        if (destMd5InDvm.equals("0")) {
            return true;
        }

        return false;
    }
}

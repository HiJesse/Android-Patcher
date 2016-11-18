package cn.jesse.patcher.loader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;

import cn.jesse.patcher.loader.app.PatcherApplication;
import cn.jesse.patcher.loader.util.IntentUtil;
import cn.jesse.patcher.loader.util.PatchFileUtil;
import cn.jesse.patcher.loader.util.PatchInfo;
import cn.jesse.patcher.loader.util.PatcherInternals;
import cn.jesse.patcher.loader.util.SecurityCheck;

/**
 * Created by jesse on 15/11/2016.
 */
public class PatcherLoader extends AbstractPatcherLoader{
    private final String TAG = Constants.LOADER_TAG + "PatcherLoader";

    private PatchInfo patchInfo;

    /**
     * only main process can handle patch version change or incomplete
     */
    @Override
    public Intent tryLoad(PatcherApplication app, int patchFlag, boolean patchLoadVerifyFlag) {
        Intent resultIntent = new Intent();

        //统计 load patch 耗时
        long begin = SystemClock.elapsedRealtime();
        tryLoadPatchFilesInternal(app, patchFlag, patchLoadVerifyFlag, resultIntent);
        long cost = SystemClock.elapsedRealtime() - begin;
        IntentUtil.setIntentPatchCostTime(resultIntent, cost);
        return resultIntent;
    }

    private void tryLoadPatchFilesInternal(PatcherApplication app, int patchFlag, boolean patchLoadVerifyFlag, Intent resultIntent) {

        //flag为disable直接return
        if (!PatcherInternals.isPatcherEnabled(patchFlag)) {
            IntentUtil.setIntentReturnCode(resultIntent, Constants.ERROR_LOAD_DISABLE);
            return;
        }

        //
        File patchDirectoryFile = PatchFileUtil.getPatchDirectory(app);
        if (patchDirectoryFile == null) {
            Log.w(TAG, "tryLoadPatchFiles:getPatchDirectory == null");
            //treat as not exist
            IntentUtil.setIntentReturnCode(resultIntent, Constants.ERROR_LOAD_PATCH_DIRECTORY_NOT_EXIST);
            return;
        }
        String patchDirectoryPath = patchDirectoryFile.getAbsolutePath();

        //检查补丁路径是否存在
        if (!patchDirectoryFile.exists()) {
            Log.w(TAG, "tryLoadPatchFiles:patch dir not exist:" + patchDirectoryPath);
            IntentUtil.setIntentReturnCode(resultIntent, Constants.ERROR_LOAD_PATCH_DIRECTORY_NOT_EXIST);
            return;
        }

        //patcher/patch.info
        File patchInfoFile = PatchFileUtil.getPatchInfoFile(patchDirectoryPath);

        //检查patch info文件是否存在
        if (!patchInfoFile.exists()) {
            Log.w(TAG, "tryLoadPatchFiles:patch info not exist:" + patchInfoFile.getAbsolutePath());
            IntentUtil.setIntentReturnCode(resultIntent, Constants.ERROR_LOAD_PATCH_INFO_NOT_EXIST);
            return;
        }

        //patch.info文件中存储了如下新旧两个版本补丁的MD5
        //old = 641e634c5b8f1649c75caf73794acbdf
        //new = 2c150d8560334966952678930ba67fa8
        File patchInfoLockFile = PatchFileUtil.getPatchInfoLockFile(patchDirectoryPath);

        //通过lockFile加锁, 检查patch info文件中的补丁版本信息
        patchInfo = PatchInfo.readAndCheckPropertyWithLock(patchInfoFile, patchInfoLockFile);
        if (patchInfo == null) {
            IntentUtil.setIntentReturnCode(resultIntent, Constants.ERROR_LOAD_PATCH_INFO_CORRUPTED);
            return;
        }

        String oldVersion = patchInfo.oldVersion;
        String newVersion = patchInfo.newVersion;

        //检查 oldVersion或newVersion是否为空
        if (oldVersion == null || newVersion == null) {
            //it is nice to clean patch
            Log.w(TAG, "tryLoadPatchFiles:onPatchInfoCorrupted");
            IntentUtil.setIntentReturnCode(resultIntent, Constants.ERROR_LOAD_PATCH_INFO_CORRUPTED);
            return;
        }

        resultIntent.putExtra(IntentUtil.INTENT_PATCH_OLD_VERSION, oldVersion);
        resultIntent.putExtra(IntentUtil.INTENT_PATCH_NEW_VERSION, newVersion);

        boolean mainProcess = PatcherInternals.isInMainProcess(app);
        boolean versionChanged = !(oldVersion.equals(newVersion));

        String version = oldVersion;
        //如果版本变化,并且当前运行在主进程,则允许加载最新补丁
        if (versionChanged && mainProcess) {
            version = newVersion;
        }

        //检查当前补丁版本的MD5标识是否为空
        if (PatcherInternals.isNullOrNil(version)) {
            Log.w(TAG, "tryLoadPatchFiles:version is blank, wait main process to restart");
            IntentUtil.setIntentReturnCode(resultIntent, Constants.ERROR_LOAD_PATCH_INFO_BLANK);
            return;
        }

        //patch-641e634c
        String patchName = PatchFileUtil.getPatchVersionDirectory(version);

        //patcher/patch.info/patch-641e634c
        String patchVersionDirectory = patchDirectoryPath + "/" + patchName;
        File patchVersionDirectoryFile = new File(patchVersionDirectory);

        //检查当前版本补丁路径是否存在
        if (!patchVersionDirectoryFile.exists()) {
            Log.w(TAG, "tryLoadPatchFiles:onPatchVersionDirectoryNotFound");
            //we may delete patch info file
            IntentUtil.setIntentReturnCode(resultIntent, Constants.ERROR_LOAD_PATCH_VERSION_DIRECTORY_NOT_EXIST);
            return;
        }

        //patcher/patch.info/patch-641e634c/patch-641e634c.apk
        File patchVersionFile = new File(patchVersionDirectoryFile.getAbsolutePath(), PatchFileUtil.getPatchVersionFile(version));

        //检查补丁文件是否存在
        if (!patchVersionFile.exists()) {
            Log.w(TAG, "tryLoadPatchFiles:onPatchVersionFileNotFound");
            //we may delete patch info file
            IntentUtil.setIntentReturnCode(resultIntent, Constants.ERROR_LOAD_PATCH_VERSION_FILE_NOT_EXIST);
            return;
        }

        //检查补丁文件签名和Patcher id是否一致
        SecurityCheck securityCheck = new SecurityCheck(app);

        int returnCode = PatcherInternals.checkPatcherPackage(app, patchFlag, patchVersionFile, securityCheck);
        if (returnCode != Constants.ERROR_PACKAGE_CHECK_OK) {
            Log.w(TAG, "tryLoadPatchFiles:checkPatcherPackage");
            resultIntent.putExtra(IntentUtil.INTENT_PATCH_PACKAGE_PATCH_CHECK, returnCode);
            IntentUtil.setIntentReturnCode(resultIntent, Constants.ERROR_LOAD_PATCH_PACKAGE_CHECK_FAIL);
            return;
        }

        resultIntent.putExtra(IntentUtil.INTENT_PATCH_PACKAGE_CONFIG, securityCheck.getPackagePropertiesIfPresent());

        final boolean isEnabledForDex = PatcherInternals.isPatcherEnabledForDex(patchFlag);

        //如果支持dex修复 则继续检查dex补丁文件是否存在
        if (isEnabledForDex) {
            //patcher/patch.info/patch-641e634c/dex
            boolean dexCheck = PatcherDexLoader.checkComplete(patchVersionDirectory, securityCheck, resultIntent);
            if (!dexCheck) {
                //file not found, do not load patch
                Log.w(TAG, "tryLoadPatchFiles:dex check fail");
                return;
            }
        }

        //如果支持so修复 则继续检查so补丁文件是否存在
        final boolean isEnabledForNativeLib = PatcherInternals.isPatcherEnabledForNativeLib(patchFlag);

        if (isEnabledForNativeLib) {
            //patcher/patch.info/patch-641e634c/lib
            boolean libCheck = PatcherSoLoader.checkComplete(patchVersionDirectory, securityCheck, resultIntent);
            if (!libCheck) {
                //file not found, do not load patch
                Log.w(TAG, "tryLoadPatchFiles:native lib check fail");
                return;
            }
        }

        //如果支持资源修复 则继续检查资源补丁文件是否存在
        final boolean isEnabledForResource = PatcherInternals.isPatcherEnabledForResource(patchFlag);
        Log.w(TAG, "tryLoadPatchFiles:isEnabledForResource:" + isEnabledForResource);
        if (isEnabledForResource) {
            boolean resourceCheck = PatcherResourceLoader.checkComplete(app, patchVersionDirectory, securityCheck, resultIntent);
            if (!resourceCheck) {
                //file not found, do not load patch
                Log.w(TAG, "tryLoadPatchFiles:resource check fail");
                return;
            }
        }

        //符合条件的话就更新版本,并将最新的patch info更新入文件
        //we should first try rewrite patch info file, if there is a error, we can't load jar
        if (mainProcess && versionChanged) {
            patchInfo.oldVersion = version;
            //update old version to new
            if (!PatchInfo.rewritePatchInfoFileWithLock(patchInfoFile, patchInfo, patchInfoLockFile)) {
                IntentUtil.setIntentReturnCode(resultIntent, Constants.ERROR_LOAD_PATCH_REWRITE_PATCH_INFO_FAIL);
                Log.w(TAG, "tryLoadPatchFiles:onReWritePatchInfoCorrupted");
                return;
            }
        }

        //检查safe mode计数是否超过三次
        if (!checkSafeModeCount(app)) {
            resultIntent.putExtra(IntentUtil.INTENT_PATCH_EXCEPTION, new PatcherRuntimeException("checkSafeModeCount fail"));
            IntentUtil.setIntentReturnCode(resultIntent, Constants.ERROR_LOAD_PATCH_UNCAUGHT_EXCEPTION);
            Log.w(TAG, "tryLoadPatchFiles:checkSafeModeCount fail");
            return;
        }

        //now we can load patch jar
        if (isEnabledForDex) {
            boolean loadTinkerJars = PatcherDexLoader.loadPatcherJars(app, patchLoadVerifyFlag, patchVersionDirectory, resultIntent);
            if (!loadTinkerJars) {
                Log.w(TAG, "tryLoadPatchFiles:onPatchLoadDexesFail");
                return;
            }
        }

        //now we can load patch resource
        if (isEnabledForResource) {
            boolean loadTinkerResources = PatcherResourceLoader.loadPatcherResources(app, patchLoadVerifyFlag, patchVersionDirectory, resultIntent);
            if (!loadTinkerResources) {
                Log.w(TAG, "tryLoadPatchFiles:onPatchLoadResourcesFail");
                return;
            }
        }
        //all is ok!
        IntentUtil.setIntentReturnCode(resultIntent, Constants.ERROR_LOAD_OK);
        Log.i(TAG, "tryLoadPatchFiles: load end, ok!");
        return;
    }

    /**
     * 检查safe mode的计数 超过3次之后 return false
     */
    private boolean checkSafeModeCount(PatcherApplication application) {
        String processName = PatcherInternals.getProcessName(application);
        String preferName = Constants.PATCHER_OWN_PREFERENCE_CONFIG + processName;
        //each process have its own SharedPreferences file
        SharedPreferences sp = application.getSharedPreferences(preferName, Context.MODE_PRIVATE);
        int count = sp.getInt(Constants.PATCHER_SAFE_MODE_COUNT, 0);
        Log.w(TAG, "PATCHER safe mode preferName:" + preferName + " count:" + count);
        if (count >= Constants.PATCHER_SAFE_MODE_MAX_COUNT) {
            sp.edit().putInt(Constants.PATCHER_SAFE_MODE_COUNT, 0).commit();
            return false;
        }
        application.setUseSafeMode(true);
        count++;
        sp.edit().putInt(Constants.PATCHER_SAFE_MODE_COUNT, count).commit();
        Log.w(TAG, "after PATCHER safe mode count:" + count);
        return true;
    }
}

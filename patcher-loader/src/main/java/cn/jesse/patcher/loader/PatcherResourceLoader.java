package cn.jesse.patcher.loader;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;

import cn.jesse.patcher.loader.util.IntentUtil;
import cn.jesse.patcher.loader.util.PatchFileUtil;
import cn.jesse.patcher.loader.util.ResPatchInfo;
import cn.jesse.patcher.loader.util.SecurityCheck;

/**
 * Created by jesse on 17/11/2016.
 */
public class PatcherResourceLoader {
    protected static final String RESOURCE_META_FILE = Constants.RES_META_FILE;
    protected static final String RESOURCE_FILE      = Constants.RES_NAME;
    protected static final String RESOURCE_PATH      = Constants.RES_PATH;
    private static final String TAG = Constants.LOADER_TAG + "ResourceLoader";
    private static ResPatchInfo resPatchInfo = new ResPatchInfo();

    /**
     * Load patcher resources
     */
    public static boolean loadPatcherResources(Context context, boolean patcherLoadVerifyFlag, String directory, Intent intentResult) {
        // 没有资源补丁
        if (resPatchInfo == null || resPatchInfo.resArscMd5 == null) {
            return true;
        }

        //拼装资源补丁的路径, 并统计资源加载的耗时
        String resourceString = directory + "/" + RESOURCE_PATH +  "/" + RESOURCE_FILE;
        File resourceFile = new File(resourceString);
        long start = System.currentTimeMillis();

        // 如果开启了补丁合法性校验, 则校验补丁文件的MD5.同Dex补丁的校验流程.
        if (patcherLoadVerifyFlag) {
            if (!PatchFileUtil.checkResourceArscMd5(resourceFile, resPatchInfo.resArscMd5)) {
                Log.e(TAG, "Failed to load resource file, path: " + resourceFile.getPath() + ", expect md5: " + resPatchInfo.resArscMd5);
                IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_VERSION_RESOURCE_MD5_MISMATCH);
                return false;
            }
            Log.i(TAG, "verify resource file:" + resourceFile.getPath() + " md5, use time: " + (System.currentTimeMillis() - start));
        }

        // 加载资源补丁, 如有问题则卸载当前补丁.
        try {
            ResLoader.monkeyPatchExistingResources(context, resourceString);
            Log.i(TAG, "monkeyPatchExistingResources resource file:" + resourceString + ", use time: " + (System.currentTimeMillis() - start));
        } catch (Throwable e) {
            Log.e(TAG, "install resources failed");
            //remove patch dex if resource is installed failed
            try {
                DexLoader.uninstallPatchDex(context.getClassLoader());
            } catch (Throwable throwable) {
                Log.e(TAG, "uninstallPatchDex failed", e);
            }
            intentResult.putExtra(IntentUtil.INTENT_PATCH_EXCEPTION, e);
            IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_VERSION_RESOURCE_LOAD_EXCEPTION);
            return false;
        }

        return true;
    }

    /**
     * resource file exist?
     * fast check, only check whether exist
     *
     * @param directory
     * @return boolean
     */
    public static boolean checkComplete(Context context, String directory, SecurityCheck securityCheck, Intent intentResult) {
        //从SecurityCheck中拿到补丁包中res_meta.txt的信息
        String meta = securityCheck.getMetaContentMap().get(RESOURCE_META_FILE);
        //not found resource
        if (meta == null) {
            return true;
        }

        //将meta中第一行中的MD5数据读取出来,并校验MD5本身是否有问题
        //only parse first line for faster
        ResPatchInfo.parseResPatchInfoFirstLine(meta, resPatchInfo);

        if (resPatchInfo.resArscMd5 == null) {
            return true;
        }
        if (!ResPatchInfo.checkResPatchInfo(resPatchInfo)) {
            intentResult.putExtra(IntentUtil.INTENT_PATCH_PACKAGE_PATCH_CHECK, Constants.ERROR_PACKAGE_CHECK_RESOURCE_META_CORRUPTED);
            IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_PACKAGE_CHECK_FAIL);
            return false;
        }

        //校验合成资源补丁的路径和文件是否存在.
        String resourcePath = directory + "/" + RESOURCE_PATH + "/";

        File resourceDir = new File(resourcePath);

        if (!resourceDir.exists() || !resourceDir.isDirectory()) {
            IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_VERSION_RESOURCE_DIRECTORY_NOT_EXIST);
            return false;
        }

        File resourceFile = new File(resourcePath + RESOURCE_FILE);
        if (!resourceFile.exists()) {
            IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_VERSION_RESOURCE_FILE_NOT_EXIST);
            return false;
        }

        //校验当前的环境是否可以做资源更新,并且为补丁的加载做预热
        try {
            ResLoader.isResourceCanPatch(context);
        } catch (Throwable e) {
            Log.e(TAG, "resource hook check failed.", e);
            intentResult.putExtra(IntentUtil.INTENT_PATCH_EXCEPTION, e);
            IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_VERSION_RESOURCE_LOAD_EXCEPTION);
            return false;
        }
        return true;
    }

}

package cn.jesse.patcher.loader;

import android.app.Application;
import android.content.Intent;

import java.util.ArrayList;
import java.util.HashMap;

import cn.jesse.patcher.loader.util.DexDiffPatchInfo;
import cn.jesse.patcher.loader.util.IntentUtil;
import cn.jesse.patcher.loader.util.PatcherInternals;
import cn.jesse.patcher.loader.util.SecurityCheck;

/**
 * Created by jesse on 17/11/2016.
 */
public class PatcherDexLoader {
    private static final String TAG = Constants.LOADER_TAG + "PatcherDexLoader";

    private static final String                           DEX_MEAT_FILE     = Constants.DEX_META_FILE;
    private static final String                           DEX_PATH          = Constants.DEX_PATH;
    private static final String                           DEX_OPTIMIZE_PATH = Constants.DEX_OPTIMIZE_PATH;
    private static final ArrayList<DexDiffPatchInfo>      dexList           = new ArrayList<>();

    private PatcherDexLoader() {
    }

    public static boolean loadPatcherJars(Application application, boolean tinkerLoadVerifyFlag, String directory, Intent intentResult, boolean isSystemOTA) {
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


        return false;
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

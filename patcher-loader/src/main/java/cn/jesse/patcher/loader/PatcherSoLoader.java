package cn.jesse.patcher.loader;

import android.content.Intent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cn.jesse.patcher.loader.util.BsDiffPatchInfo;
import cn.jesse.patcher.loader.util.IntentUtil;
import cn.jesse.patcher.loader.util.SecurityCheck;

/**
 * Created by jesse on 17/11/2016.
 */
public class PatcherSoLoader {
    protected static final String SO_MEAT_FILE = Constants.SO_META_FILE;
    protected static final String SO_PATH      = Constants.SO_PATH;
    private static final   String TAG          = Constants.LOADER_TAG + "PatcherSoLoader";

    /**
     * all the library files in meta file exist?
     * fast check, only check whether exist
     *
     * @param directory
     * @return boolean
     */
    public static boolean checkComplete(String directory, SecurityCheck securityCheck, Intent intentResult) {
        String meta = securityCheck.getMetaContentMap().get(SO_MEAT_FILE);
        //not found lib
        // 先判断checkComplete时so_meta.txt是否有效
        if (meta == null) {
            return true;
        }
        ArrayList<BsDiffPatchInfo> libraryList = new ArrayList<>();

        // 将so_meta.txt中的数据解析到array中
        BsDiffPatchInfo.parseDiffPatchInfo(meta, libraryList);

        if (libraryList.isEmpty()) {
            return true;
        }

        //patcher/patch-xxx/lib
        String libraryPath = directory + "/" + SO_PATH + "/";

        HashMap<String, String> libs = new HashMap<>();

        // 遍历SO数组
        for (BsDiffPatchInfo info : libraryList) {
            // SO本身文件信息是否合法
            if (!BsDiffPatchInfo.checkDiffPatchInfo(info)) {
                intentResult.putExtra(IntentUtil.INTENT_PATCH_PACKAGE_PATCH_CHECK, Constants.ERROR_PACKAGE_CHECK_LIB_META_CORRUPTED);
                IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_PACKAGE_CHECK_FAIL);
                return false;
            }
            String middle = info.path + "/" + info.name;

            //unlike dex, keep the original structure
            // 将合法的SO存入Map中
            libs.put(middle, info.md5);
        }

        File libraryDir = new File(libraryPath);

        // 校验补丁路径 /patcher/patch-xxx/lib是否存在
        if (!libraryDir.exists() || !libraryDir.isDirectory()) {
            IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_VERSION_LIB_DIRECTORY_NOT_EXIST);
            return false;
        }

        //fast check whether there is any dex files missing
        // 校验补丁信息中记录的SO文件,在补丁路径下是否物理存在.
        for (String relative : libs.keySet()) {
            File libFile = new File(libraryPath + relative);
            if (!libFile.exists()) {
                IntentUtil.setIntentReturnCode(intentResult, Constants.ERROR_LOAD_PATCH_VERSION_LIB_FILE_NOT_EXIST);
                intentResult.putExtra(IntentUtil.INTENT_PATCH_MISSING_LIB_PATH, libFile.getAbsolutePath());
                return false;
            }
        }

        //if is ok, add to result intent
        // 将合法补丁的Map 存入result中,供加载SO补丁时使用.
        intentResult.putExtra(IntentUtil.INTENT_PATCH_LIBS_PATH, libs);
        return true;
    }
}

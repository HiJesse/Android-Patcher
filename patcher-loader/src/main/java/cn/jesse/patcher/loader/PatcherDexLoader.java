package cn.jesse.patcher.loader;

import android.app.Application;
import android.content.Intent;

import cn.jesse.patcher.loader.util.SecurityCheck;

/**
 * Created by jesse on 17/11/2016.
 */
public class PatcherDexLoader {

    public static boolean loadPatcherJars(Application application, boolean tinkerLoadVerifyFlag, String directory, Intent intentResult) {
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
        return false;
    }
}

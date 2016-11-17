package cn.jesse.patcher.loader;

import android.content.Context;
import android.content.Intent;

import cn.jesse.patcher.loader.util.SecurityCheck;

/**
 * Created by jesse on 17/11/2016.
 */
public class PatcherResourceLoader {

    /**
     * Load tinker resources
     */
    public static boolean loadPatcherResources(Context context, boolean patcherLoadVerifyFlag, String directory, Intent intentResult) {
        return false;
    }

    /**
     * resource file exist?
     * fast check, only check whether exist
     *
     * @param directory
     * @return boolean
     */
    public static boolean checkComplete(Context context, String directory, SecurityCheck securityCheck, Intent intentResult) {
        return false;
    }

}

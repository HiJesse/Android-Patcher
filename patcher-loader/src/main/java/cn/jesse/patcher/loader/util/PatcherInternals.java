package cn.jesse.patcher.loader.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cn.jesse.patcher.loader.Constants;

/**
 * Created by jesse on 15/11/2016.
 */
public class PatcherInternals {
    private static final String TAG = Constants.LOADER_TAG + "Internals";
    private static final boolean VM_IS_ART = isVmArt(System.getProperty("java.vm.version"));
    /**
     * or you may just hardcode them in your app
     */
    private static String processName = null;
    private static String patcherID = null;

    public static boolean isVmArt() {
        return VM_IS_ART || Build.VERSION.SDK_INT >= 21;
    }

    public static boolean isSystemOTA(String lastFingerPrint) {
        String currentFingerprint = Build.FINGERPRINT;
        if (lastFingerPrint == null
                || lastFingerPrint.equals("")
                || currentFingerprint == null
                || currentFingerprint.equals("")) {
            Log.d(TAG, "fingerprint empty:" + lastFingerPrint + ",current:" + currentFingerprint);
            return false;
        } else {
            if (lastFingerPrint.equals(currentFingerprint)) {
                Log.d(TAG, "same fingerprint:" + currentFingerprint);
                return false;
            } else {
                Log.d(TAG, "system OTA,fingerprint not equal:" + lastFingerPrint + "," + currentFingerprint);
                return true;
            }
        }
    }

    public static boolean isNullOrNil(final String object) {
        if ((object == null) || (object.length() <= 0)) {
            return true;
        }
        return false;
    }

    /**
     * thinker package check
     * @param context
     * @param patcherFlag
     * @param patchFile
     * @param securityCheck
     * @return
     */
    public static int checkPatcherPackage(Context context, int patcherFlag, File patchFile, SecurityCheck securityCheck) {
        int returnCode = checkSignatureAndPatcherID(context, patchFile, securityCheck);
        if (returnCode == Constants.ERROR_PACKAGE_CHECK_OK) {
            returnCode = checkPackageAndPatcherFlag(securityCheck, patcherFlag);
        }
        return returnCode;
    }
    /**
     * check patch file signature and PATCHER_ID
     *
     * @param context
     * @param patchFile
     * @param securityCheck
     * @return
     */
    public static int checkSignatureAndPatcherID(Context context, File patchFile, SecurityCheck securityCheck) {
        if (!securityCheck.verifyPatchMetaSignature(patchFile)) {
            return Constants.ERROR_PACKAGE_CHECK_SIGNATURE_FAIL;
        }

        String oldPatcherId = getManifestPatcherID(context);
        if (oldPatcherId == null) {
            return Constants.ERROR_PACKAGE_CHECK_APK_PATCHER_ID_NOT_FOUND;
        }

        HashMap<String, String> properties = securityCheck.getPackagePropertiesIfPresent();

        if (properties == null) {
            return Constants.ERROR_PACKAGE_CHECK_PACKAGE_META_NOT_FOUND;
        }

        String patchPatcherId = properties.get(Constants.PATCHER_ID);
        if (patchPatcherId == null) {
            return Constants.ERROR_PACKAGE_CHECK_PATCH_PATCHER_ID_NOT_FOUND;
        }
        if (!oldPatcherId.equals(patchPatcherId)) {
            Log.e(TAG, "patcherId is not equal, base is " + oldPatcherId + ", but patch is " + patchPatcherId);
            return Constants.ERROR_PACKAGE_CHECK_PATCHER_ID_NOT_EQUAL;
        }
        return Constants.ERROR_PACKAGE_CHECK_OK;
    }


    public static int checkPackageAndPatcherFlag(SecurityCheck securityCheck, int patcherFlag) {
        if (isPatcherEnabledAll(patcherFlag)) {
            return Constants.ERROR_PACKAGE_CHECK_OK;
        }
        HashMap<String, String> metaContentMap = securityCheck.getMetaContentMap();
        //check dex
        boolean dexEnable = isPatcherEnabledForDex(patcherFlag);
        if (!dexEnable && metaContentMap.containsKey(Constants.DEX_META_FILE)) {
            return Constants.ERROR_PACKAGE_CHECK_PATCHERFLAG_NOT_SUPPORT;
        }
        //check native library
        boolean nativeEnable = isPatcherEnabledForNativeLib(patcherFlag);
        if (!nativeEnable && metaContentMap.containsKey(Constants.SO_META_FILE)) {
            return Constants.ERROR_PACKAGE_CHECK_PATCHERFLAG_NOT_SUPPORT;
        }
        //check resource
        boolean resEnable = isPatcherEnabledForResource(patcherFlag);
        if (!resEnable && metaContentMap.containsKey(Constants.RES_META_FILE)) {
            return Constants.ERROR_PACKAGE_CHECK_PATCHERFLAG_NOT_SUPPORT;
        }

        return Constants.ERROR_PACKAGE_CHECK_OK;
    }

    /**
     * not like {@cod SecurityCheck.getPackagePropertiesIfPresent}
     * we don't check Signatures or other files, we just get the package meta's properties directly
     * @param patchFile
     * @return
     */
    public static Properties fastGetPatchPackageMeta(File patchFile) {
        if (patchFile == null || !patchFile.isFile() || patchFile.length() == 0) {
            Log.e(TAG, "patchFile is illegal");
            return null;
        }
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(patchFile);
            ZipEntry packageEntry = zipFile.getEntry(Constants.PACKAGE_META_FILE);
            if (packageEntry == null) {
                Log.e(TAG, "patch meta entry not found");
                return null;
            }
            InputStream inputStream = null;
            try {
                inputStream = zipFile.getInputStream(packageEntry);
                Properties properties = new Properties();
                properties.load(inputStream);
                return properties;
            } finally {
                PatchFileUtil.closeQuietly(inputStream);
            }
        } catch (IOException e) {
            Log.e(TAG, "fastGetPatchPackageMeta exception:" + e.getMessage());
            return null;
        } finally {
            PatchFileUtil.closeZip(zipFile);
        }
    }
    public static String getManifestPatcherID(Context context) {
        if (patcherID != null) {
            return patcherID;
        }
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);

            Object object = appInfo.metaData.get(Constants.PATCHER_ID);
            if (object != null) {
                patcherID = String.valueOf(object);
            } else {
                patcherID = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "getManifestPatcherID exception:" + e.getMessage());
            return null;
        }
        return patcherID;
    }

    public static boolean isPatcherEnabledForDex(int flag) {
        return (flag & Constants.PATCHER_DEX_MASK) != 0;
    }

    public static boolean isPatcherEnabledForNativeLib(int flag) {
        return (flag & Constants.PATCHER_NATIVE_LIBRARY_MASK) != 0;
    }

    public static boolean isPatcherEnabledForResource(int flag) {
        //FIXME:res flag depends dex flag
        return (flag & Constants.PATCHER_RESOURCE_MASK) != 0;
    }

    public static String getTypeString(int type) {
        switch (type) {
            case Constants.TYPE_DEX:
                return "dex";
            case Constants.TYPE_DEX_FOR_ART:
                return "dex_art";
            case Constants.TYPE_DEX_OPT:
                return "dex_opt";
            case Constants.TYPE_LIBRARY:
                return "lib";
            case Constants.TYPE_PATCH_FILE:
                return "patch_file";
            case Constants.TYPE_PATCH_INFO:
                return "patch_info";
            case Constants.TYPE_RESOURCE:
                return "resource";
            default:
                return "unknown";
        }
    }

    /**
     * you can set Patcher disable in runtime at some times!
     * @param context
     */
    public static void setPatcherDisableWithSharedPreferences(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.PATCHER_SHARE_PREFERENCE_CONFIG, Context.MODE_MULTI_PROCESS);
        sp.edit().putBoolean(Constants.PATCHER_ENABLE_CONFIG, false).commit();
    }

    /**
     * can't load or receive any patch!
     * @param context
     * @return
     */
    public static boolean isPatcherEnableWithSharedPreferences(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.PATCHER_SHARE_PREFERENCE_CONFIG, Context.MODE_MULTI_PROCESS);
        return sp.getBoolean(Constants.PATCHER_ENABLE_CONFIG, true);
    }

    public static boolean isPatcherEnabled(int flag) {
        return (flag != Constants.PATCHER_DISABLE);
    }

    public static boolean isPatcherEnabledAll(int flag) {
        return (flag == Constants.PATCHER_ENABLE_ALL);
    }

    public static boolean isInMainProcess(Context context) {
        String pkgName = context.getPackageName();
        String processName = getProcessName(context);
        if (processName == null || processName.length() == 0) {
            processName = "";
        }

        return pkgName.equals(processName);
    }

    public static void killAllOtherProcess(Context context) {
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // NOTE: getRunningAppProcess() ONLY GIVE YOU THE PROCESS OF YOUR OWN PACKAGE IN ANDROID M
        // BUT THAT'S ENOUGH HERE
        for (ActivityManager.RunningAppProcessInfo ai : am.getRunningAppProcesses()) {
            // KILL OTHER PROCESS OF MINE
            if (ai.uid == android.os.Process.myUid() && ai.pid != android.os.Process.myPid()) {
                android.os.Process.killProcess(ai.pid);
            }
        }

    }

    /**
     * add process name cache
     *
     * @param context
     * @return
     */
    public static String getProcessName(final Context context) {
        if (processName != null) {
            return processName;
        }
        //will not null
        processName = getProcessNameInternal(context);
        return processName;
    }


    private static String getProcessNameInternal(final Context context) {
        int myPid = android.os.Process.myPid();

        if (context == null || myPid <= 0) {
            return "";
        }

        ActivityManager.RunningAppProcessInfo myProcess = null;
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        try {
            for (ActivityManager.RunningAppProcessInfo process : activityManager.getRunningAppProcesses()) {
                if (process.pid == myPid) {
                    myProcess = process;
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getProcessNameInternal exception:" + e.getMessage());
        }

        if (myProcess != null) {
            return myProcess.processName;
        }

        byte[] b = new byte[128];
        FileInputStream in = null;
        try {
            in = new FileInputStream("/proc/" + myPid + "/cmdline");
            int len = in.read(b);
            if (len > 0) {
                for (int i = 0; i < len; i++) { // lots of '0' in tail , remove them
                    if (b[i] > 128 || b[i] <= 0) {
                        len = i;
                        break;
                    }
                }
                return new String(b, 0, len);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
            }
        }

        return "";
    }

    /**
     * vm whether it is art
     * @return
     */
    private static boolean isVmArt(String versionString) {
        boolean isArt = false;
        if (versionString != null) {
            Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
            if (matcher.matches()) {
                try {
                    int major = Integer.parseInt(matcher.group(1));
                    int minor = Integer.parseInt(matcher.group(2));
                    isArt = (major > 2)
                            || ((major == 2)
                            && (minor >= 1));
                } catch (NumberFormatException e) {
                    // let isMultidexCapable be false
                }
            }
        }
        return isArt;
    }
}

package cn.jesse.patcher.loader.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import cn.jesse.patcher.loader.Constants;
import cn.jesse.patcher.loader.PatcherRuntimeException;

/**
 * Created by jesse on 17/11/2016.
 */
public class PatchInfo {
    public static final int    MAX_EXTRACT_ATTEMPTS = Constants.MAX_EXTRACT_ATTEMPTS;
    public static final String OLD_VERSION          = Constants.OLD_VERSION;
    public static final String NEW_VERSION          = Constants.NEW_VERSION;
    private static final String TAG = Constants.LOADER_TAG + "PatchInfo";
    public String oldVersion;
    public String newVersion;

    public PatchInfo(String oldVer, String newVew) {
        // TODO Auto-generated constructor stub
        this.oldVersion = oldVer;
        this.newVersion = newVew;
    }

    public static PatchInfo readAndCheckPropertyWithLock(File pathInfoFile, File lockFile) {
        File lockParentFile = lockFile.getParentFile();
        if (!lockParentFile.exists()) {
            lockParentFile.mkdirs();
        }

        PatchInfo patchInfo;
        FileLockHelper fileLock = null;
        try {
            fileLock = FileLockHelper.getFileLock(lockFile);
            patchInfo = readAndCheckProperty(pathInfoFile);
        } catch (Exception e) {
            throw new PatcherRuntimeException("readAndCheckPropertyWithLock fail", e);
        } finally {
            try {
                if (fileLock != null) {
                    fileLock.close();
                }
            } catch (IOException e) {
                Log.i(TAG, "releaseInfoLock error", e);
            }
        }

        return patchInfo;
    }

    public static boolean rewritePatchInfoFileWithLock(File pathInfoFile, PatchInfo info, File lockFile) {
        File lockParentFile = lockFile.getParentFile();
        if (!lockParentFile.exists()) {
            lockParentFile.mkdirs();
        }
        boolean rewriteSuccess;
        FileLockHelper fileLock = null;
        try {
            fileLock = FileLockHelper.getFileLock(lockFile);
            rewriteSuccess = rewritePatchInfoFile(pathInfoFile, info);
        } catch (Exception e) {
            throw new PatcherRuntimeException("rewritePatchInfoFileWithLock fail", e);
        } finally {
            try {
                if (fileLock != null) {
                    fileLock.close();
                }
            } catch (IOException e) {
                Log.i(TAG, "releaseInfoLock error", e);
            }

        }
        return rewriteSuccess;
    }

    private static PatchInfo readAndCheckProperty(File pathInfoFile) {
        boolean isReadPatchSuccessful = false;
        int numAttempts = 0;
        String oldVer = null;
        String newVer = null;

        while (numAttempts < MAX_EXTRACT_ATTEMPTS && !isReadPatchSuccessful) {
            numAttempts++;
            Properties properties = new Properties();
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(pathInfoFile);
                properties.load(inputStream);
                oldVer = properties.getProperty(OLD_VERSION);
                newVer = properties.getProperty(NEW_VERSION);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                PatchFileUtil.closeQuietly(inputStream);
            }

            if (oldVer == null || newVer == null) {
                continue;
            }
            //oldver may be "" or 32 md5
            if ((!oldVer.equals("") && !PatchFileUtil.checkIfMd5Valid(oldVer)) || !PatchFileUtil.checkIfMd5Valid(newVer)) {
                Log.w(TAG, "path info file  corrupted:" + pathInfoFile.getAbsolutePath());
                continue;
            } else {
                isReadPatchSuccessful = true;
            }
        }

        if (isReadPatchSuccessful) {
            return new PatchInfo(oldVer, newVer);
        }

        return null;
    }

    private static boolean rewritePatchInfoFile(File pathInfoFile, PatchInfo info) {
        if (pathInfoFile == null || info == null) {
            return false;
        }
        Log.i(TAG, "rewritePatchInfoFile file path:"
                + pathInfoFile.getAbsolutePath()
                + " , oldVer:"
                + info.oldVersion
                + ", newVer:"
                + info.newVersion);

        boolean isWritePatchSuccessful = false;
        int numAttempts = 0;

        File parentFile = pathInfoFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        while (numAttempts < MAX_EXTRACT_ATTEMPTS && !isWritePatchSuccessful) {
            numAttempts++;

            Properties newProperties = new Properties();
            newProperties.put(OLD_VERSION, info.oldVersion);
            newProperties.put(NEW_VERSION, info.newVersion);
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(pathInfoFile, false);
                String comment = "from old version:" + info.oldVersion + " to new version:" + info.newVersion;
                newProperties.store(outputStream, comment);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                PatchFileUtil.closeQuietly(outputStream);
            }

            PatchInfo tempInfo = readAndCheckProperty(pathInfoFile);

            isWritePatchSuccessful = tempInfo != null && tempInfo.oldVersion.equals(info.oldVersion) && tempInfo.newVersion.equals(info.newVersion);
            if (!isWritePatchSuccessful) {
                pathInfoFile.delete();
            }
        }
        if (isWritePatchSuccessful) {
            return true;
        }

        return false;
    }
}

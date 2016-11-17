package cn.jesse.patcher.loader.util;

import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

import cn.jesse.patcher.loader.Constants;

/**
 * Created by jesse on 17/11/2016.
 */
public class FileLockHelper implements Closeable{
    public static final int MAX_LOCK_ATTEMPTS   = 3;
    public static final int LOCK_WAIT_EACH_TIME = 10;
    private static final String TAG = Constants.LOADER_TAG + "FileLockHelper";
    private final FileOutputStream outputStream;
    private final FileLock fileLock;

    /**
     * 申请文件使用权限,失败后睡10ms重试知道成功或失败超过3次
     */
    private FileLockHelper(File lockFile) throws IOException {
        outputStream = new FileOutputStream(lockFile);

        int numAttempts = 0;
        boolean isGetLockSuccess;
        FileLock localFileLock = null;
        //just wait twice,
        Exception saveException = null;
        while (numAttempts < MAX_LOCK_ATTEMPTS) {
            numAttempts++;
            try {
                localFileLock = outputStream.getChannel().lock();
                isGetLockSuccess = (localFileLock != null);
                if (isGetLockSuccess) {
                    break;
                }
                //it can just sleep 0, afraid of cpu scheduling
                Thread.sleep(LOCK_WAIT_EACH_TIME);

            } catch (Exception e) {
//                e.printStackTrace();
                saveException = e;
                Log.e(TAG, "getInfoLock Thread failed time:" + LOCK_WAIT_EACH_TIME);
            }
        }

        if (localFileLock == null) {
            throw new IOException("Patcher Exception:FileLockHelper lock file failed: " + lockFile.getAbsolutePath(), saveException);
        }
        fileLock = localFileLock;
    }

    public static FileLockHelper getFileLock(File lockFile) throws IOException {
        return new FileLockHelper(lockFile);
    }

    @Override
    public void close() throws IOException {
        try {
            if (fileLock != null) {
                fileLock.release();
            }
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}

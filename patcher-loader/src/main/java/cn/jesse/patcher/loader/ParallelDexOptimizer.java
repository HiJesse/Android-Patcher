package cn.jesse.patcher.loader;

import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import cn.jesse.patcher.loader.util.PatchFileUtil;
import dalvik.system.DexFile;

/**
 * Created by jesse on 21/11/2016.
 */
public class ParallelDexOptimizer {
    private static final String TAG = "ParallelDexOptimizer";

    /**
     * Optimize (trigger dexopt or dex2oat) dexes.
     *
     * @param dexFiles
     * @param optimizedDir
     * @param cb
     *
     * @return
     *  If all dexes are optimized successfully, return true. Otherwise return false.
     */
    public synchronized static boolean optimizeAll(File[] dexFiles, File optimizedDir, ResultCallback cb) {
        final AtomicInteger successCount = new AtomicInteger(0);
        return optimizeAllLocked(Arrays.asList(dexFiles), optimizedDir, successCount, cb);
    }

    /**
     * Optimize (trigger dexopt or dex2oat) dexes.
     *
     * @param dexFiles
     * @param optimizedDir
     * @param cb
     *
     * @return
     *  If all dexes are optimized successfully, return true. Otherwise return false.
     */
    public synchronized static boolean optimizeAll(Collection<File> dexFiles, File optimizedDir, ResultCallback cb) {
        final AtomicInteger successCount = new AtomicInteger(0);
        return optimizeAllLocked(dexFiles, optimizedDir, successCount, cb);
    }

    private static boolean optimizeAllLocked(Collection<File> dexFiles, File optimizedDir, AtomicInteger successCount, ResultCallback cb) {
        final CountDownLatch lauch = new CountDownLatch(dexFiles.size());
        final ExecutorService threadPool = Executors.newCachedThreadPool();
        long startTick = System.nanoTime();
        for (File dexFile : dexFiles) {
            OptimizeWorker worker = new OptimizeWorker(dexFile, optimizedDir, successCount, lauch, cb);
            threadPool.submit(worker);
        }
        try {
            //挂起,直到count == 0
            lauch.await();
            long timeCost = (System.nanoTime() - startTick) / 1000000;
            if (successCount.get() == dexFiles.size()) {
                Log.i(TAG, "All dexes are optimized successfully, cost: " + timeCost + " ms.");
                return true;
            } else {
                Log.e(TAG, "Dexes optimizing failed, some dexes are not optimized.");
                return false;
            }
        } catch (InterruptedException e) {
            Log.w(TAG, "Dex optimizing was interrupted.", e);
            return false;
        } finally {
            threadPool.shutdown();
        }
    }

    public interface ResultCallback {
        void onSuccess(File dexFile, File optimizedDir);
        void onFailed(File dexFile, File optimizedDir, Throwable thr);
    }

    private static class OptimizeWorker implements Runnable {
        private final File dexFile;
        private final File optimizedDir;
        private final AtomicInteger successCount;
        private final CountDownLatch waitingLauch;
        private final ResultCallback callback;

        OptimizeWorker(File dexFile, File optimizedDir, AtomicInteger successCount, CountDownLatch lauch, ResultCallback cb) {
            this.dexFile = dexFile;
            this.optimizedDir = optimizedDir;
            this.successCount = successCount;
            this.waitingLauch = lauch;
            this.callback = cb;
        }

        @Override
        public void run() {
            try {
                DexFile.loadDex(dexFile.getAbsolutePath(), PatchFileUtil.optimizedPathFor(this.dexFile, this.optimizedDir), 0);
                successCount.incrementAndGet();
                if (callback != null) {
                    callback.onSuccess(dexFile, optimizedDir);
                }
            } catch (final Exception e) {
                Log.e(TAG, "Failed to optimize dex: " + dexFile.getAbsolutePath(), e);
                if (callback != null) {
                    callback.onFailed(dexFile, optimizedDir, e);
                }
            } finally {
                this.waitingLauch.countDown();
            }
        }
    }
}

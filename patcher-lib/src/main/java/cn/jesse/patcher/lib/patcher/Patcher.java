package cn.jesse.patcher.lib.patcher;

import android.content.Context;
import android.content.Intent;

import java.io.File;

import cn.jesse.patcher.lib.util.PatcherLog;
import cn.jesse.patcher.loader.Constants;
import cn.jesse.patcher.loader.PatcherRuntimeException;
import cn.jesse.patcher.loader.util.PatchFileUtil;
import cn.jesse.patcher.loader.util.PatcherInternals;

/**
 * Created by jesse on 05/12/2016.
 */

public class Patcher {
    private static final String TAG = Constants.LOADER_TAG + "Patcher";

    private static Patcher sInstance;
    private static boolean sInstalled = false;

    final Context context;
    /**
     * data dir, such as /data/data/package/patcher
     */
    final File patchDirectory;
    final PatchListener listener;
    final LoadReporter  loadReporter;
    final PatchReporter patchReporter;
    final File          patchInfoFile;
    final boolean       isMainProcess;
    final boolean       isPatchProcess;
    /**
     * same with {@code PatcherApplication.patcherLoadVerifyFlag}
     */
    final boolean       patcherLoadVerifyFlag;

    /**
     * same with {@code PatcherApplication.patcherFlags}
     */
    int              patcherFlags;
    PatcherLoadResult patcherLoadResult;
    /**
     * whether load patch success
     */
    private boolean loaded = false;

    private Patcher(Context context, int patcherFlags, LoadReporter loadReporter, PatchReporter patchReporter,
                   PatchListener listener, File patchDirectory, File patchInfoFile,
                   boolean isInMainProc, boolean isPatchProcess, boolean patcherLoadVerifyFlag) {
        this.context = context;
        this.listener = listener;
        this.loadReporter = loadReporter;
        this.patchReporter = patchReporter;
        this.patcherFlags = patcherFlags;
        this.patchDirectory = patchDirectory;
        this.patchInfoFile = patchInfoFile;
        this.isMainProcess = isInMainProc;
        this.patcherLoadVerifyFlag = patcherLoadVerifyFlag;
        this.isPatchProcess = isPatchProcess;
    }

    /**
     * init with default config patcher
     * for safer, you must use @{link PatcherInstaller.install} first!
     *
     * @param context we will use the application context
     * @return the Patcher object
     */
    public static Patcher with(Context context) {
        if (!sInstalled) {
            throw new PatcherRuntimeException("you must install patcher before get patcher sInstance");
        }
        if (sInstance == null) {
            synchronized (Patcher.class) {
                if (sInstance == null) {
                    sInstance = new Builder(context).build();
                }
            }
        }
        return sInstance;
    }

    /**
     * create custom patcher by {@link Patcher.Builder}
     * please do it when very first your app start.
     *
     * @param patcher
     */
    public static void create(Patcher patcher) {
        if (sInstance != null) {
            throw new PatcherRuntimeException("Patcher instance is already set.");
        }
        sInstance = patcher;
    }

    /**
     * you must install patcher first!!
     *
     * @param intentResult
     * @param serviceClass
     * @param upgradePatch
     * @param repairPatch
     */
    public void install(Intent intentResult, Class<? extends AbstractResultService> serviceClass,
                        AbstractPatch upgradePatch, AbstractPatch repairPatch
    ) {
        sInstalled = true;
        PatcherPatchService.setPatchProcessor(upgradePatch, repairPatch, serviceClass);

        if (!isPatcherEnabled()) {
            PatcherLog.e(TAG, "patcher is disabled");
            return;
        }
        if (intentResult == null) {
            throw new PatcherRuntimeException("intentResult must not be null.");
        }
        patcherLoadResult = new PatcherLoadResult();
        patcherLoadResult.parsePatcherResult(getContext(), intentResult);
        //after load code set
        loadReporter.onLoadResult(patchDirectory, patcherLoadResult.loadCode, patcherLoadResult.costTime);

        if (!loaded) {
            PatcherLog.w(TAG, "patcher load fail!");
        }
    }

    /**
     * set patcherPatchServiceNotificationId
     *
     * @param id
     */
    public void setPatchServiceNotificationId(int id) {
        PatcherPatchService.setPatcherNotificationId(id);
    }


    /**
     * Nullable, should check the loaded flag first
     */
    public PatcherLoadResult getPatcherLoadResultIfPresent() {
        return patcherLoadResult;
    }

    public void install(Intent intentResult) {
        install(intentResult, DefaultPatcherResultService.class, new UpgradePatch(), new RepairPatch());
    }

    public Context getContext() {
        return context;
    }

    public boolean isMainProcess() {
        return isMainProcess;
    }

    public boolean isPatchProcess() {
        return isPatchProcess;
    }

    public void setPatcherDisable() {
        patcherFlags = Constants.PATCHER_DISABLE;
    }

    public LoadReporter getLoadReporter() {
        return loadReporter;
    }

    public PatchReporter getPatchReporter() {
        return patchReporter;
    }


    public boolean isPatcherEnabled() {
        return PatcherInternals.isPatcherEnabled(patcherFlags);
    }

    public boolean isPatcherLoaded() {
        return loaded;
    }

    public void setPatcherLoaded(boolean isLoaded) {
        loaded = isLoaded;
    }

    public boolean isPatcherInstalled() {
        return sInstalled;
    }

    public boolean isPatcherLoadVerify() {
        return patcherLoadVerifyFlag;
    }

    public boolean isEnabledForDex() {
        return PatcherInternals.isPatcherEnabledForDex(patcherFlags);
    }

    public boolean isEnabledForNativeLib() {
        return PatcherInternals.isPatcherEnabledForNativeLib(patcherFlags);
    }

    public boolean isEnabledForResource() {
        return PatcherInternals.isPatcherEnabledForResource(patcherFlags);
    }

    public File getPatchDirectory() {
        return patchDirectory;
    }

    public File getPatchInfoFile() {
        return patchInfoFile;
    }

    public PatchListener getPatchListener() {
        return listener;
    }


    public int getPatcherFlags() {
        return patcherFlags;
    }

    /**
     * clean all patch files
     */
    public void cleanPatch() {
        if (patchDirectory == null) {
            return;
        }
        if (isPatcherLoaded()) {
            PatcherLog.e(TAG, "it is not safety to clean patch when patcher is loaded, you should kill all your process after clean!");
        }
        PatchFileUtil.deleteDir(patchDirectory);
    }

    /**
     * clean the patch version files, such as patcher/patch-641e634c
     *
     * @param versionName
     */
    public void cleanPatchByVersion(String versionName) {
        if (patchDirectory == null || versionName == null) {
            return;
        }
        String path = patchDirectory.getAbsolutePath() + "/" + versionName;
        PatchFileUtil.deleteDir(path);
    }

    /**
     * get the rom size of patcher, use kb
     *
     * @return
     */
    public long getPatcherRomSpace() {
        if (patchDirectory == null) {
            return 0;
        }

        return PatchFileUtil.getFileOrDirectorySize(patchDirectory) / 1024;
    }

    /**
     * try delete the temp version files
     *
     * @param patchFile
     */
    public void cleanPatchByVersion(File patchFile) {
        if (patchDirectory == null || patchFile == null || !patchFile.exists()) {
            return;
        }
        String versionName = PatchFileUtil.getPatchVersionDirectory(PatchFileUtil.getMD5(patchFile));
        cleanPatchByVersion(versionName);
    }


    public static class Builder {
        private final Context context;
        private final boolean mainProcess;
        private final boolean patchProcess;

        private int status = -1;
        private LoadReporter  loadReporter;
        private PatchReporter patchReporter;
        private PatchListener listener;
        private File          patchDirectory;
        private File          patchInfoFile;
        private Boolean       patcherLoadVerifyFlag;

        /**
         * Start building a new {@link Patcher} instance.
         */
        public Builder(Context context) {
            if (context == null) {
                throw new PatcherRuntimeException("Context must not be null.");
            }
            this.context = context;
            this.mainProcess = PatcherServiceInternals.isInMainProcess(context);
            this.patchProcess = PatcherServiceInternals.isInPatcherPatchServiceProcess(context);
            this.patchDirectory = PatchFileUtil.getPatchDirectory(context);
            if (this.patchDirectory == null) {
                PatcherLog.e(TAG, "patchDirectory is null!");
                return;
            }
            this.patchInfoFile = PatchFileUtil.getPatchInfoFile(patchDirectory.getAbsolutePath());
            PatcherLog.w(TAG, "patcher patch directory: %s", patchDirectory);
        }

        public Builder patcherFlags(int patcherFlags) {
            if (this.status != -1) {
                throw new PatcherRuntimeException("patcherFlag is already set.");
            }
            this.status = patcherFlags;
            return this;
        }

        public Builder patcherLoadVerifyFlag(Boolean verifyMd5WhenLoad) {
            if (verifyMd5WhenLoad == null) {
                throw new PatcherRuntimeException("patcherLoadVerifyFlag must not be null.");
            }
            if (this.patcherLoadVerifyFlag != null) {
                throw new PatcherRuntimeException("patcherLoadVerifyFlag is already set.");
            }
            this.patcherLoadVerifyFlag = verifyMd5WhenLoad;
            return this;
        }

        public Builder loadReport(LoadReporter loadReporter) {
            if (loadReporter == null) {
                throw new PatcherRuntimeException("loadReporter must not be null.");
            }
            if (this.loadReporter != null) {
                throw new PatcherRuntimeException("loadReporter is already set.");
            }
            this.loadReporter = loadReporter;
            return this;
        }

        public Builder patchReporter(PatchReporter patchReporter) {
            if (patchReporter == null) {
                throw new PatcherRuntimeException("patchReporter must not be null.");
            }
            if (this.patchReporter != null) {
                throw new PatcherRuntimeException("patchReporter is already set.");
            }
            this.patchReporter = patchReporter;
            return this;
        }

        public Builder listener(PatchListener listener) {
            if (listener == null) {
                throw new PatcherRuntimeException("listener must not be null.");
            }
            if (this.listener != null) {
                throw new PatcherRuntimeException("listener is already set.");
            }
            this.listener = listener;
            return this;
        }

        public Patcher build() {
            if (status == -1) {
                status = Constants.PATCHER_ENABLE_ALL;
            }

            if (loadReporter == null) {
                loadReporter = new DefaultLoadReporter(context);
            }

            if (patchReporter == null) {
                patchReporter = new DefaultPatchReporter(context);
            }

            if (listener == null) {
                listener = new DefaultPatchListener(context);
            }

            if (patcherLoadVerifyFlag == null) {
                patcherLoadVerifyFlag = false;
            }

            return new Patcher(context, status, loadReporter, patchReporter, listener, patchDirectory,
                    patchInfoFile, mainProcess, patchProcess, patcherLoadVerifyFlag);
        }
    }
}

package cn.jesse.patcher.loader.app;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;

/**
 * Created by jesse on 15/11/2016.
 */
public abstract class AbsApplicationProxy implements ApplicationProxy {
    private final Application application;
    private final Intent patcherResultIntent;
    private final long        applicationStartElapsedTime;
    private final long        applicationStartMillisTime;
    private final int         patchFlags;
    private final boolean     patchLoadVerifyFlag;
    private Resources[]    resources;
    private ClassLoader[]  classLoader;
    private AssetManager[] assetManager;

    public AbsApplicationProxy(Application application, int patchFlags, boolean patchLoadVerifyFlag,
                               long applicationStartElapsedTime, long applicationStartMillisTime, Intent patcherResultIntent,
                               Resources[] resources, ClassLoader[] classLoader, AssetManager[] assetManager) {
        this.application = application;
        this.patchFlags = patchFlags;
        this.patchLoadVerifyFlag = patchLoadVerifyFlag;
        this.applicationStartElapsedTime = applicationStartElapsedTime;
        this.applicationStartMillisTime = applicationStartMillisTime;
        this.patcherResultIntent = patcherResultIntent;
        this.resources = resources;
        this.classLoader = classLoader;
        this.assetManager = assetManager;
    }

    public void setResources(Resources resources) {
        this.resources[0] = resources;
    }

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager[0] = assetManager;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader[0] = classLoader;
    }

    public Application getApplication() {
        return application;
    }

    public Intent getPatcherResultIntent() {
        return patcherResultIntent;
    }

    public int getPatchFlags() {
        return patchFlags;
    }

    public boolean isPatchLoadVerifyFlag() {
        return patchLoadVerifyFlag;
    }

    public long getApplicationStartElapsedTime() {
        return applicationStartElapsedTime;
    }

    public long getApplicationStartMillisTime() {
        return applicationStartMillisTime;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onLowMemory() {

    }

    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public void onTerminate() {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public void onBaseContextAttached(Context base) {

    }
}

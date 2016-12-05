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
    //Application,resource,assetManager,classLoader的引用
    private final Application application;
    private Resources[]    resources;
    private ClassLoader[]  classLoader;
    private AssetManager[] assetManager;

    //用于记录启动加载patcher loader的状态
    private final Intent patcherResultIntent;

    //系统的存活时间和app启动时刻
    private final long applicationStartElapsedTime;
    private final long applicationStartMillisTime;

    //注解中的两个flags
    private final int patchFlags;
    private final boolean patchLoadVerifyFlag;


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

    public boolean getPatchLoadVerifyFlag() {
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

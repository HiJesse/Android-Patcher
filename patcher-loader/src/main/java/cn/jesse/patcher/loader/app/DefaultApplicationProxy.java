package cn.jesse.patcher.loader.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

/**
 * Created by jesse on 15/11/2016.
 */
public class DefaultApplicationProxy extends AbsApplicationProxy {
    private final String TAG = "Patcher." + getClass().getSimpleName();

    public DefaultApplicationProxy(Application application, int patchFlags, boolean patchLoadVerifyFlag,
                                   long applicationStartElapsedTime, long applicationStartMillisTime, Intent patcherResultIntent,
                                   Resources[] resources, ClassLoader[] classLoader, AssetManager[] assetManager) {
        super(application, patchFlags, patchLoadVerifyFlag,
                applicationStartElapsedTime, applicationStartMillisTime, patcherResultIntent,
                resources, classLoader, assetManager);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory level:" + level);
    }

    @Override
    public void onTerminate() {
        Log.d(TAG, "onTerminate");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged:" + newConfig.toString());
    }

    @Override
    public void onBaseContextAttached(Context base) {
        Log.d(TAG, "onBaseContextAttached:");
    }
}

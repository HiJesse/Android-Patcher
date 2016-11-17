package cn.jesse.patchersample;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

//import cn.jesse.patcher.anno.PatcherApplication;
import cn.jesse.patcher.loader.Constants;
import cn.jesse.patcher.loader.app.DefaultApplicationProxy;

/**
 * Created by jesse on 14/11/2016.
 */
//@PatcherApplication(
//        application = "cn.jesse.patchersample.MyApplication",
//        flags = Constants.PATCHER_ENABLE_ALL,
//        loadVerifyFlag = false
//)
public class ApplicationProxy extends DefaultApplicationProxy{
    private final String TAG = ApplicationProxy.class.getSimpleName();

    public ApplicationProxy(Application application, int patchFlags, boolean patchLoadVerifyFlag,
                            long applicationStartElapsedTime, long applicationStartMillisTime, Intent patcherResultIntent,
                            Resources[] resources, ClassLoader[] classLoader, AssetManager[] assetManager) {
        super(application, patchFlags, patchLoadVerifyFlag,
                applicationStartElapsedTime, applicationStartMillisTime, patcherResultIntent,
                resources, classLoader, assetManager);
    }

    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        Log.d(TAG, getApplicationStartElapsedTime() + "");
        Log.d(TAG, getApplicationStartMillisTime() + "");
        Log.d(TAG, getPatcherResultIntent().getExtras().toString());
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
    }
}

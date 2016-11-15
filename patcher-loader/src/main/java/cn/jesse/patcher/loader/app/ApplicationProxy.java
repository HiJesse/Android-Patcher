package cn.jesse.patcher.loader.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

/**
 * the proxy of {@link android.app.Application}
 * Created by jesse on 15/11/2016.
 */
public interface ApplicationProxy {

    /**
     * Same as {@link Application#onCreate()}.
     */
    void onCreate();

    /**
     * Same as {@link Application#onLowMemory()}.
     */
    void onLowMemory();

    /**
     * Same as {@link Application#onTrimMemory(int level)}.
     * @param level
     */
    void onTrimMemory(int level);

    /**
     * Same as {@link Application#onTerminate()}.
     */
    void onTerminate();

    /**
     * Same as {@link Application#onConfigurationChanged(Configuration newconfig)}.
     */
    void onConfigurationChanged(Configuration newConfig);

    /**
     * Same as {@link Application#attachBaseContext(Context context)}.
     */
    void onBaseContextAttached(Context base);
}

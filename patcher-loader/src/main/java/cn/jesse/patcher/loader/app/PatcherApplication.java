package cn.jesse.patcher.loader.app;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.SystemClock;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import cn.jesse.patcher.loader.Constants;
import cn.jesse.patcher.loader.PatcherLoader;
import cn.jesse.patcher.loader.PatcherRuntimeException;
import cn.jesse.patcher.loader.util.IntentUtil;
import cn.jesse.patcher.loader.util.PatcherInternals;
import cn.jesse.patcher.loader.util.ReflectUtil;

/**
 * Created by jesse on 15/11/2016.
 */
public class PatcherApplication extends Application {
    private static final String PATCHER_LOADER_METHOD   = "tryLoad";
    /**
     * patcherFlags, which types is supported
     * dex only, library only, all support
     * default: PATCHER_ENABLE_ALL
     */
    private final int patcherFlags;
    /**
     * whether verify md5 when we load dex or lib
     * they store at data/data/package, and we had verity them at the :patch process.
     * so we don't have to verity them every time for quicker!
     * default:false
     */
    private final boolean patcherLoadVerifyFlag;
    private final String  delegateClassName;
    private final String  loaderClassName;
    /**
     * if we have load patch, we should use safe mode
     */
    private boolean useSafeMode;
    private Intent PatcherResultIntent;

    private Object         delegate      = null;
    private Resources[]    resources     = new Resources[1];
    private ClassLoader[]  classLoader   = new ClassLoader[1];
    private AssetManager[] assetManager  = new AssetManager[1];

    private long applicationStartElapsedTime;
    private long applicationStartMillisTime;

    /**
     * current build.
     */
    protected PatcherApplication(int patcherFlags) {
        this(patcherFlags, "cn.jesse.patcher.loader.app.DefaultApplicationProxy", PatcherLoader.class.getName(), false);
    }

    /**
     * @param delegateClassName The fully-qualified name of the {@link ApplicationProxy} class
     *                          that will act as the delegate for application lifecycle callbacks.
     */
    protected PatcherApplication(int patcherFlags, String delegateClassName,
                                String loaderClassName, boolean patcherLoadVerifyFlag) {
        this.patcherFlags = patcherFlags;
        this.delegateClassName = delegateClassName;
        this.loaderClassName = loaderClassName;
        this.patcherLoadVerifyFlag = patcherLoadVerifyFlag;

    }

    protected PatcherApplication(int patcherFlags, String delegateClassName) {
        this(patcherFlags, delegateClassName, PatcherLoader.class.getName(), false);
    }

    private Object createDelegate() {
        try {
            // Use reflection to create the delegate so it doesn't need to go into the primary dex.
            // And we can also patch it
            Class<?> delegateClass = Class.forName(delegateClassName, false, getClassLoader());
            Constructor<?> constructor = delegateClass.getConstructor(Application.class, int.class, boolean.class, long.class, long.class,
                    Intent.class, Resources[].class, ClassLoader[].class, AssetManager[].class);
            return constructor.newInstance(this, patcherFlags, patcherLoadVerifyFlag,
                    applicationStartElapsedTime, applicationStartMillisTime,
                    PatcherResultIntent, resources, classLoader, assetManager);
        } catch (Throwable e) {
            throw new PatcherRuntimeException("createDelegate failed", e);
        }
    }

    private synchronized void ensureDelegate() {
        if (delegate == null) {
            delegate = createDelegate();
        }
    }

    /**
     * Hook for sub-classes to run logic after the {@link Application#attachBaseContext} has been
     * called but before the delegate is created. Implementors should be very careful what they do
     * here since {@link android.app.Application#onCreate} will not have yet been called.
     */
    private void onBaseContextAttached(Context base) {
        applicationStartElapsedTime = SystemClock.elapsedRealtime();
        applicationStartMillisTime = System.currentTimeMillis();
        loadPatcher();
        ensureDelegate();
        try {
            Method method = ReflectUtil.findMethod(delegate, "onBaseContextAttached", Context.class);
            method.invoke(delegate, base);
        } catch (Throwable t) {
            throw new PatcherRuntimeException("onBaseContextAttached method not found", t);
        }
        //reset save mode
        if (useSafeMode) {
            String processName = PatcherInternals.getProcessName(this);
            String preferName = Constants.PATCHER_OWN_PREFERENCE_CONFIG + processName;
            SharedPreferences sp = getSharedPreferences(preferName, Context.MODE_PRIVATE);
            sp.edit().putInt(Constants.PATCHER_SAFE_MODE_COUNT, 0).commit();
        }
    }

    @Override
    protected final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        onBaseContextAttached(base);
    }

    private void loadPatcher() {
        //disable patcher, not need to install
        if (patcherFlags == Constants.PATCHER_DISABLE) {
            return;
        }
        PatcherResultIntent = new Intent();
        try {
            //reflect loader, because loaderClass may be define by user!
            Class<?> loadClass = Class.forName(loaderClassName, false, getClassLoader());

            Method loadMethod = loadClass.getMethod(PATCHER_LOADER_METHOD, PatcherApplication.class, int.class, boolean.class);
            Constructor<?> constructor = loadClass.getConstructor();
            PatcherResultIntent = (Intent) loadMethod.invoke(constructor.newInstance(), this, patcherFlags, patcherLoadVerifyFlag);
        } catch (Throwable e) {
            //has exception, put exception error code
            IntentUtil.setIntentReturnCode(PatcherResultIntent, Constants.ERROR_LOAD_PATCH_UNKNOWN_EXCEPTION);
            PatcherResultIntent.putExtra(IntentUtil.INTENT_PATCH_EXCEPTION, e);
        }
    }

    private void delegateMethod(String methodName) {
        if (delegate != null) {
            try {
                Method method = ReflectUtil.findMethod(delegate, methodName, new Class[0]);
                method.invoke(delegate, new Object[0]);
            } catch (Throwable t) {
                throw new PatcherRuntimeException(String.format("%s method not found", methodName), t);
            }
        }
    }

    @Override
    public final void onCreate() {
        super.onCreate();
        ensureDelegate();
        delegateMethod("onCreate");
    }

    @Override
    public final void onTerminate() {
        super.onTerminate();
        delegateMethod("onTerminate");
    }

    @Override
    public final void onLowMemory() {
        super.onLowMemory();
        delegateMethod("onLowMemory");
    }

    private void delegateTrimMemory(int level) {
        if (delegate != null) {
            try {
                Method method = ReflectUtil.findMethod(delegate, "onTrimMemory", int.class);
                method.invoke(delegate, level);
            } catch (Throwable t) {
                throw new PatcherRuntimeException("onTrimMemory method not found", t);
            }
        }
    }

    @TargetApi(14)
    @Override
    public final void onTrimMemory(int level) {
        super.onTrimMemory(level);
        delegateTrimMemory(level);
    }

    private void delegateConfigurationChanged(Configuration newConfig) {
        if (delegate != null) {
            try {
                Method method = ReflectUtil.findMethod(delegate, "onConfigurationChanged", Configuration.class);
                method.invoke(delegate, newConfig);
            } catch (Throwable t) {
                throw new PatcherRuntimeException("onConfigurationChanged method not found", t);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        delegateConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        if (resources[0] != null) {
            return resources[0];
        }
        return super.getResources();
    }

    @Override
    public ClassLoader getClassLoader() {
        if (classLoader[0] != null) {
            return classLoader[0];
        }
        return super.getClassLoader();
    }

    @Override
    public AssetManager getAssets() {
        if (assetManager[0] != null) {
            return assetManager[0];
        }
        return super.getAssets();
    }

    public void setUseSafeMode(boolean useSafeMode) {
        this.useSafeMode = useSafeMode;
    }
}

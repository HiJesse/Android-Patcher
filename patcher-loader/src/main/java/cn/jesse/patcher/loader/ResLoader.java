package cn.jesse.patcher.loader;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.jesse.patcher.loader.util.ReflectUtil;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;

/**
 * Created by jesse on 24/11/2016.
 */
public class ResLoader {
    private static final String TAG               = Constants.LOADER_TAG + "ResourcePatcher";
    private static final String TEST_ASSETS_VALUE = "only_use_to_test_patcher_resource.txt";

    // original value
    private static Collection<WeakReference<Resources>> references;
    private static AssetManager newAssetManager          = null;
    private static Method addAssetPathMethod       = null;
    private static Method       ensureStringBlocksMethod = null;
    private static Field assetsFiled              = null;
    private static Field        resourcesImplFiled       = null;
    private static Field        resDir                   = null;
    private static Field        packagesFiled            = null;
    private static Field        resourcePackagesFiled    = null;

    public static void isResourceCanPatch(Context context) throws Throwable {
        //   - Replace mResDir to point to the external resource file instead of the .apk. This is
        //     used as the asset path for new Resources objects.
        //   - Set Application#mLoadedApk to the found LoadedApk instance
        // 校验当前系统环境是否支持资源补丁的加载, 并保存下来加载补丁所需的Field和Method, 为资源补丁加载预热

        // Find the ActivityThread instance for the current thread
        Class<?> activityThread = Class.forName("android.app.ActivityThread");
        // API version 8 has PackageInfo, 10 has LoadedApk. 9, I don't know.
        //android 2.3之前是ActivityThread$PackageInfo, 之后就单独抽离出来了LoadedApk
        Class<?> loadedApkClass;
        try {
            loadedApkClass = Class.forName("android.app.LoadedApk");
        } catch (ClassNotFoundException e) {
            loadedApkClass = Class.forName("android.app.ActivityThread$PackageInfo");
        }
        Field mApplication = loadedApkClass.getDeclaredField("mApplication");
        mApplication.setAccessible(true);
        //拿到mResDir,mPackages和mResourcePackages,并设置访问权限
        resDir = loadedApkClass.getDeclaredField("mResDir");
        resDir.setAccessible(true);
        packagesFiled = activityThread.getDeclaredField("mPackages");
        packagesFiled.setAccessible(true);

        resourcePackagesFiled = activityThread.getDeclaredField("mResourcePackages");
        resourcePackagesFiled.setAccessible(true);
        /*
        (Note: the resource directory is *also* inserted into the loadedApk in
        monkeyPatchApplication)
        The code seems to perform this:
        File externalResourceFile = <path to resources.ap_ or extracted directory>

        AssetManager newAssetManager = new AssetManager();
        newAssetManager.addAssetPath(externalResourceFile)

        // Kitkat needs this method call, Lollipop doesn't. However, it doesn't seem to cause any harm
        // in L, so we do it unconditionally.
        newAssetManager.ensureStringBlocks();

        // Find the singleton instance of ResourcesManager
        ResourcesManager resourcesManager = ResourcesManager.getInstance();

        // Iterate over all known Resources objects
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (WeakReference<Resources> wr : resourcesManager.mActiveResources.values()) {
                Resources resources = wr.get();
                // Set the AssetManager of the Resources instance to our brand new one
                resources.mAssets = newAssetManager;
                resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
            }
        }

        // Also, for each context, call getTheme() to get the current theme; null out its
        // mTheme field, then invoke initializeTheme() to force it to be recreated (with the
        // new asset manager!)

        */
        // Create a new AssetManager instance and point it to the resources installed under
        // /sdcard
        AssetManager assets = context.getAssets();
        // 区分一些修改了AssetManager的ROM,并创建出来新的实例
        if (assets.getClass().getName().equals("android.content.res.BaiduAssetManager")) {
            Class baiduAssetManager = Class.forName("android.content.res.BaiduAssetManager");
            newAssetManager = (AssetManager) baiduAssetManager.getConstructor().newInstance();
        } else {
            newAssetManager = AssetManager.class.getConstructor().newInstance();
        }

        //拿到addAssetPath方法,并设置访问权限
        addAssetPathMethod = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
        addAssetPathMethod.setAccessible(true);

        // Kitkat needs this method call, Lollipop doesn't. However, it doesn't seem to cause any harm
        // in L, so we do it unconditionally.
        ensureStringBlocksMethod = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
        ensureStringBlocksMethod.setAccessible(true);

        // Iterate over all known Resources objects
        //Resources对象在KITKAT之下是以HashMap的类型作为ActivityThread类的属性
        //其余的系统版本都是以ArrayMap被ResourcesManager持有的.所以要按照系统区分开.
        if (SDK_INT >= KITKAT) {
            //pre-N
            // Find the singleton instance of ResourcesManager
            //得到ResourcesManager对象并实例化,通过mActiveResources属性拿到Resources的容器
            Class<?> resourcesManagerClass = Class.forName("android.app.ResourcesManager");
            Method mGetInstance = resourcesManagerClass.getDeclaredMethod("getInstance");
            mGetInstance.setAccessible(true);
            Object resourcesManager = mGetInstance.invoke(null);
            try {
                Field fMActiveResources = resourcesManagerClass.getDeclaredField("mActiveResources");
                fMActiveResources.setAccessible(true);
                ArrayMap<?, WeakReference<Resources>> arrayMap =
                        (ArrayMap<?, WeakReference<Resources>>) fMActiveResources.get(resourcesManager);
                references = arrayMap.values();
            } catch (NoSuchFieldException ignore) {
                // N moved the resources to mResourceReferences
                //在Android 7.0之后根据源码变动改为拿mResourceReferences属性
                Field mResourceReferences = resourcesManagerClass.getDeclaredField("mResourceReferences");
                mResourceReferences.setAccessible(true);
                //noinspection unchecked
                references = (Collection<WeakReference<Resources>>) mResourceReferences.get(resourcesManager);
            }
        } else {
            //Android 4.4以下Resources相关引用是在ActivityThread的mActiveResources属性
            Field fMActiveResources = activityThread.getDeclaredField("mActiveResources");
            fMActiveResources.setAccessible(true);
            Object thread = getActivityThread(context, activityThread);
            @SuppressWarnings("unchecked")
            HashMap<?, WeakReference<Resources>> map =
                    (HashMap<?, WeakReference<Resources>>) fMActiveResources.get(thread);
            references = map.values();
        }
        // check resource
        if (references == null || references.isEmpty()) {
            throw new IllegalStateException("resource references is null or empty");
        }
        try {
            assetsFiled = Resources.class.getDeclaredField("mAssets");
            assetsFiled.setAccessible(true);
        } catch (Throwable ignore) {
            // N moved the mAssets inside an mResourcesImpl field
            // N 开始Android源码中又抽出了一个ResourcesImpl类, 这个类中持有AssetManager
            resourcesImplFiled = Resources.class.getDeclaredField("mResourcesImpl");
            resourcesImplFiled.setAccessible(true);
        }
    }

    public static void monkeyPatchExistingResources(Context context, String externalResourceFile) throws Throwable {
        if (externalResourceFile == null) {
            return;
        }
        // Find the ActivityThread instance for the current thread
        // 拿到当前主线程的ActivityThread对象
        Class<?> activityThread = Class.forName("android.app.ActivityThread");
        Object currentActivityThread = getActivityThread(context, activityThread);

        // 遍历ActivityThread对象中两个持有LoadedApk容器的对象
        for (Field field : new Field[]{packagesFiled, resourcePackagesFiled}) {
            Object value = field.get(currentActivityThread);

            // 遍历容器中的每一个LoadedApk对象 并将mResDir属性的值替换成资源补丁包的路径.
            for (Map.Entry<String, WeakReference<?>> entry
                    : ((Map<String, WeakReference<?>>) value).entrySet()) {
                Object loadedApk = entry.getValue().get();
                if (loadedApk == null) {
                    continue;
                }
                if (externalResourceFile != null) {
                    resDir.set(loadedApk, externalResourceFile);
                }
            }
        }
        // Create a new AssetManager instance and point it to the resources installed under
        // /sdcard
        // 将补丁包中的资源load进新的AssetManager中.
        if (((Integer) addAssetPathMethod.invoke(newAssetManager, externalResourceFile)) == 0) {
            throw new IllegalStateException("Could not create new AssetManager");
        }

        // Kitkat needs this method call, Lollipop doesn't. However, it doesn't seem to cause any harm
        // in L, so we do it unconditionally.
        ensureStringBlocksMethod.invoke(newAssetManager);

        // 遍历Resources容器, 将每个Resources对象中引用的AssetManager替换成加载过补丁资源的新的AssetManager对象.
        for (WeakReference<Resources> wr : references) {
            Resources resources = wr.get();
            //pre-N
            if (resources != null) {
                // Set the AssetManager of the Resources instance to our brand new one
                try {
                    assetsFiled.set(resources, newAssetManager);
                } catch (Throwable ignore) {
                    // N
                    // N 中先从ResourcesImpl对象中获取AssetManager对象再替换
                    Object resourceImpl = resourcesImplFiled.get(resources);
                    // for Huawei HwResourcesImpl
                    Field implAssets = ReflectUtil.findField(resourceImpl, "mAssets");
                    implAssets.setAccessible(true);
                    implAssets.set(resourceImpl, newAssetManager);
                }

                resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
            }
        }

        if (!checkResUpdate(context)) {
            throw new PatcherRuntimeException(Constants.CHECK_RES_INSTALL_FAIL);
        }
    }

    /**
     * 从asset目录下读取一个测试文件, 该测试文件在生成资源补丁的时候自动打入到asset下
     * 如果打开失败说明资源补丁没有加载成功
     */
    private static boolean checkResUpdate(Context context) {
        try {
            Log.e(TAG, "checkResUpdate success, found test resource assets file " + TEST_ASSETS_VALUE);
            context.getAssets().open(TEST_ASSETS_VALUE);
        } catch (Throwable e) {
            Log.e(TAG, "checkResUpdate failed, can't find test resource assets file " + TEST_ASSETS_VALUE + " e:" + e.getMessage());
            return false;
        }
        return true;
    }

    private static Object getActivityThread(Context context,
                                            Class<?> activityThread) {
        try {
            if (activityThread == null) {
                activityThread = Class.forName("android.app.ActivityThread");
            }
            Method m = activityThread.getMethod("currentActivityThread");
            m.setAccessible(true);
            Object currentActivityThread = m.invoke(null);
            if (currentActivityThread == null && context != null) {
                // In older versions of Android (prior to frameworks/base 66a017b63461a22842)
                // the currentActivityThread was built on thread locals, so we'll need to try
                // even harder
                Field mLoadedApk = context.getClass().getField("mLoadedApk");
                mLoadedApk.setAccessible(true);
                Object apk = mLoadedApk.get(context);
                Field mActivityThreadField = apk.getClass().getDeclaredField("mActivityThread");
                mActivityThreadField.setAccessible(true);
                currentActivityThread = mActivityThreadField.get(apk);
            }
            return currentActivityThread;
        } catch (Throwable ignore) {
            return null;
        }
    }
}

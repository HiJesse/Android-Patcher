package cn.jesse.patchersample;


import cn.jesse.patcher.loader.Constants;
import cn.jesse.patcher.loader.PatcherLoader;
import cn.jesse.patcher.loader.app.PatcherApplication;

/**
 * Created by jesse on 16/11/2016.
 */
public class MyApplication extends PatcherApplication {

    public MyApplication() {
        super(Constants.PATCHER_ENABLE_ALL, "cn.jesse.patchersample.ApplicationProxy", PatcherLoader.class.getName(), false);
    }

}

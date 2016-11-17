package cn.jesse.patcher.loader;

import android.content.Intent;

import cn.jesse.patcher.loader.app.PatcherApplication;

/**
 * Created by jesse on 17/11/2016.
 */
public abstract class AbstractPatcherLoader {
    abstract public Intent tryLoad(PatcherApplication app, int patchFlag, boolean patchLoadVerifyFlag);
}

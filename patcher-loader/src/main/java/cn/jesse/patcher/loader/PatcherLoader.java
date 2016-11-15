package cn.jesse.patcher.loader;

import android.content.Intent;
import android.os.SystemClock;

import cn.jesse.patcher.loader.app.PatcherApplication;

/**
 * Created by jesse on 15/11/2016.
 */
public class PatcherLoader {
    public Intent tryLoad(PatcherApplication app, int tinkerFlag, boolean tinkerLoadVerifyFlag) {
        Intent resultIntent = new Intent();

        long begin = SystemClock.elapsedRealtime();
        long cost = SystemClock.elapsedRealtime() - begin;
        return resultIntent;
    }
}

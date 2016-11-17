package cn.jesse.patcher.loader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import cn.jesse.patcher.loader.app.PatcherApplication;
import cn.jesse.patcher.loader.util.PatcherInternals;

/**
 * Created by jesse on 15/11/2016.
 */
public class PatcherLoader extends AbstractPatcherLoader{
    private final String TAG = Constants.LOADER_TAG + "PatcherLoader";
    
    public Intent tryLoad(PatcherApplication app, int patchFlag, boolean patchLoadVerifyFlag) {
        Intent resultIntent = new Intent();

        long begin = SystemClock.elapsedRealtime();
        long cost = SystemClock.elapsedRealtime() - begin;
        return resultIntent;
    }

    /**
     * 检查safe mode的计数 超过3次之后 return false
     */
    private boolean checkSafeModeCount(PatcherApplication application) {
        String processName = PatcherInternals.getProcessName(application);
        String preferName = Constants.PATCHER_OWN_PREFERENCE_CONFIG + processName;
        //each process have its own SharedPreferences file
        SharedPreferences sp = application.getSharedPreferences(preferName, Context.MODE_PRIVATE);
        int count = sp.getInt(Constants.PATCHER_SAFE_MODE_COUNT, 0);
        Log.w(TAG, "PATCHER safe mode preferName:" + preferName + " count:" + count);
        if (count >= Constants.PATCHER_SAFE_MODE_MAX_COUNT) {
            sp.edit().putInt(Constants.PATCHER_SAFE_MODE_COUNT, 0).commit();
            return false;
        }
        application.setUseSafeMode(true);
        count++;
        sp.edit().putInt(Constants.PATCHER_SAFE_MODE_COUNT, count).commit();
        Log.w(TAG, "after PATCHER safe mode count:" + count);
        return true;
    }
}

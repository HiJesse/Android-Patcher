package cn.jesse.patcher.lib.util;

import cn.jesse.patcher.loader.Constants;

/**
 * Created by jesse on 04/12/2016.
 */

public class PatcherLog {
    private static final String TAG = Constants.LOADER_TAG + "TinkerLog";
    private static PatcherLogImp debugLog = new PatcherLogImp() {

        @Override
        public void v(final String tag, final String msg, final Object... obj) {
            String log = obj == null ? msg : String.format(msg, obj);
            android.util.Log.v(tag, log);
        }

        @Override
        public void i(final String tag, final String msg, final Object... obj) {
            String log = obj == null ? msg : String.format(msg, obj);
            android.util.Log.i(tag, log);

        }

        @Override
        public void d(final String tag, final String msg, final Object... obj) {
            String log = obj == null ? msg : String.format(msg, obj);
            android.util.Log.d(tag, log);
        }

        @Override
        public void w(final String tag, final String msg, final Object... obj) {
            String log = obj == null ? msg : String.format(msg, obj);
            android.util.Log.w(tag, log);
        }

        @Override
        public void e(final String tag, final String msg, final Object... obj) {
            String log = obj == null ? msg : String.format(msg, obj);
            android.util.Log.e(tag, log);
        }

        @Override
        public void printErrStackTrace(String tag, Throwable tr, String format, Object... obj) {
            String log = obj == null ? format : String.format(format, obj);
            if (log == null) {
                log = "";
            }
            log += "  " + android.util.Log.getStackTraceString(tr);
            android.util.Log.e(tag, log);
        }
    };
    private static PatcherLogImp patcherLogImp = debugLog;

    public static void setTinkerLogImp(PatcherLogImp imp) {
        patcherLogImp = imp;
    }

    public static PatcherLogImp getImpl() {
        return patcherLogImp;
    }

    public static void v(final String tag, final String msg, final Object... obj) {
        if (patcherLogImp != null) {
            patcherLogImp.v(tag, msg, obj);
        }
    }

    public static void e(final String tag, final String msg, final Object... obj) {
        if (patcherLogImp != null) {
            patcherLogImp.e(tag, msg, obj);
        }
    }

    public static void w(final String tag, final String msg, final Object... obj) {
        if (patcherLogImp != null) {
            patcherLogImp.w(tag, msg, obj);
        }
    }

    public static void i(final String tag, final String msg, final Object... obj) {
        if (patcherLogImp != null) {
            patcherLogImp.i(tag, msg, obj);
        }
    }

    public static void d(final String tag, final String msg, final Object... obj) {
        if (patcherLogImp != null) {
            patcherLogImp.d(tag, msg, obj);
        }
    }

    public static void printErrStackTrace(String tag, Throwable tr, final String format, final Object... obj) {
        if (patcherLogImp != null) {
            patcherLogImp.printErrStackTrace(tag, tr, format, obj);
        }
    }

    public interface PatcherLogImp {

        void v(final String tag, final String msg, final Object... obj);

        void i(final String tag, final String msg, final Object... obj);

        void w(final String tag, final String msg, final Object... obj);

        void d(final String tag, final String msg, final Object... obj);

        void e(final String tag, final String msg, final Object... obj);

        void printErrStackTrace(String tag, Throwable tr, final String format, final Object... obj);

    }
}

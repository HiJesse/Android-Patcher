package cn.jesse.patcher.build.patch;

import java.io.File;
import java.io.IOException;

import cn.jesse.patcher.build.info.InfoWriter;
import cn.jesse.patcher.build.util.TypedValue;

/**
 * Created by jesse on 19/12/2016.
 */
public class Logger {
    private static InfoWriter logWriter;

    public static void initLogger(Configuration config) throws IOException {
        String logPath = config.mOutFolder + File.separator + TypedValue.FILE_LOG;
        logWriter = new InfoWriter(config, logPath);
    }

    public static void closeLogger() {
        if (logWriter != null) {
            logWriter.close();
        }
    }

    public static void d(final String msg) {
        Logger.d(msg, new Object[]{});
    }

    public static void d(final String format, final Object... obj) {

        String log = obj.length == 0 ? format : String.format(format, obj);
        if (log == null) {
            log = "";
        }
        //add \n
        System.out.printf(log + "\n");
        System.out.flush();

        logWriter.writeLineToInfoFile(log);
    }

    public static void e(final String msg) {
        Logger.e(msg, new Object[]{});
    }

    public static void e(final String format, final Object... obj) {
        String log = obj.length == 0 ? format : String.format(format, obj);
        if (log == null) {
            log = "";
        }
        //add \n
        System.err.printf(log + "\n");
        System.err.flush();

        logWriter.writeLineToInfoFile(log);
    }
}

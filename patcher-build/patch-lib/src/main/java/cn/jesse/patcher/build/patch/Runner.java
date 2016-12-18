package cn.jesse.patcher.build.patch;

import java.io.IOException;

import cn.jesse.patcher.build.util.PatchException;

/**
 * Created by jesse on 18/12/2016.
 */

public class Runner {
    public static final int ERRNO_ERRORS = 1;
    public static final int ERRNO_USAGE  = 2;

    protected static long mBeginTime;
    protected Configuration config;

    public static void gradleRun(InputParam inputParam) {
        mBeginTime = System.currentTimeMillis();
        Runner m = new Runner();
        m.run(inputParam);
    }

    private void run(InputParam inputParam) {
        loadConfigFromGradle(inputParam);
//        try {
//            Logger.initLogger(config);
//            patch();
//        } catch (IOException e) {
//            e.printStackTrace();
//            goToError();
//        } finally {
//            Logger.closeLogger();
//        }
    }

    private void loadConfigFromGradle(InputParam inputParam) {
        try {
            config = new Configuration(inputParam);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PatchException e) {
            e.printStackTrace();
        }
    }

    public double diffTimeFromBegin() {
        long end = System.currentTimeMillis();
        return (end - mBeginTime) / 1000.0;
    }
}

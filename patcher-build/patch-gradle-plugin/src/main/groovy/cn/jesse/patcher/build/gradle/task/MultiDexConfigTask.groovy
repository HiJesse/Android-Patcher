package cn.jesse.patcher.build.gradle.task

import cn.jesse.patcher.build.gradle.PatcherPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction;

/**
 * Created by jesse on 16/12/2016.
 */

public class MultiDexConfigTask extends DefaultTask {
    static final String MULTIDEX_CONFIG_PATH = PatcherPlugin.PATCHER_INTERMEDIATES + "patcher_multidexkeep.pro"
    static final String MULTIDEX_CONFIG_SETTINGS =
            "-keep public class * implements cn.jesse.patcher.loader.app.ApplicationProxy {\n" +
                    "    *;\n" +
                    "}\n" +
                    "\n" +
                    "-keep public class * extends cn.jesse.patcher.loader.PatcherLoader {\n" +
                    "    *;\n" +
                    "}\n" +
                    "\n" +
                    "-keep public class * extends cn.jesse.patcher.loader.app.PatcherApplication {\n" +
                    "    *;\n" +
                    "}\n"


    def applicationVariant

    public MultiDexConfigTask() {
        group = PatcherPlugin.PATCHER_PLUGIN_GROUP
    }

    @TaskAction
    def updatePatcherProguardConfig() {

    }
}

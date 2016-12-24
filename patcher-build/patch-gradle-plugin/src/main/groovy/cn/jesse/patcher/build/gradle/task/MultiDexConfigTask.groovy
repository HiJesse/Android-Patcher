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
        def file = project.file(MULTIDEX_CONFIG_PATH)
        project.logger.error("try update patcher multidex keep proguard file with ${file}")

        // Create the directory if it doesn't exist already
        file.getParentFile().mkdirs()

        // Write our recommended proguard settings to this file
        FileWriter fr = new FileWriter(file.path)

        fr.write(MULTIDEX_CONFIG_SETTINGS)
        fr.write("\n")

        // 将dex.loader中配置的class也keep进main dex
        //unlike proguard, if loader endwith *, we must change to **
        fr.write("#your dex.loader patterns here\n")
        Iterable<String> loader = project.patcher.dex.loader
        for (String pattern : loader) {
            if (pattern.endsWith("*")) {
                if (!pattern.endsWith("**")) {
                    pattern += "*"
                }
            }
            fr.write("-keep class " + pattern + " {\n" +
                    "    *;\n" +
                    "}\n")
            fr.write("\n")
        }
        fr.close()
    }
}

package cn.jesse.patcher.build.gradle.task

import cn.jesse.patcher.build.gradle.PatcherPlugin;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * Created by jesse on 16/12/2016.
 */

public class ProguardConfigTask extends DefaultTask {
    static final String PROGUARD_CONFIG_PATH =  PatcherPlugin.PATCHER_INTERMEDIATES + "patcher_proguard.pro"
    static final String PROGUARD_CONFIG_SETTINGS =
            "-keepattributes *Annotation* \n" +
                    "-dontwarn cn.jesse.patcher.anno.PatcherProcessor \n" +
//                    "-dontwarn ${AuxiliaryClassInjector.NOT_EXISTS_CLASSNAME} \n" +
                    "-keep @cn.jesse.patcher.anno.PatcherApplication public class *\n" +
                    "-keep public class * extends android.app.Application {\n" +
                    "    *;\n" +
                    "}\n" +
                    "\n" +
                    "-keep public class cn.jesse.patcher.loader.app.ApplicationProxy {\n" +
                    "    *;\n" +
                    "}\n" +
                    "-keep public class * implements cn.jesse.patcher.loader.app.ApplicationProxy {\n" +
                    "    *;\n" +
                    "}\n" +
                    "\n" +
                    "-keep public class cn.jesse.patcher.loader.PatcherLoader {\n" +
                    "    *;\n" +
                    "}\n" +
                    "-keep public class * extends cn.jesse.patcher.loader.PatcherLoader {\n" +
                    "    *;\n" +
                    "}\n" +
                    "-keep public class cn.jesse.patcher.loader.PatcherTestDexLoad {\n" +
                    "    *;\n" +
                    "}\n" +
                    "\n"


    def applicationVariant
    boolean shouldApplyMapping = true;


    public ProguardConfigTask() {
        group = PatcherPlugin.PATCHER_PLUGIN_GROUP
    }

    @TaskAction
    def updatePatcherProguardConfig() {
        println(PROGUARD_CONFIG_SETTINGS)
    }
}

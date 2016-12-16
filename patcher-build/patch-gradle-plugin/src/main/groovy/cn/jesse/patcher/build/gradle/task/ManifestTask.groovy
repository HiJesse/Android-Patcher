package cn.jesse.patcher.build.gradle.task

import cn.jesse.patcher.build.gradle.PatcherPlugin;
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction;

/**
 * Created by jesse on 16/12/2016.
 */

public class ManifestTask extends DefaultTask{
    static final String MANIFEST_XML = PatcherPlugin.PATCHER_INTERMEDIATES + "AndroidManifest.xml"
    static final String PATCHER_ID = "PATCHER_ID"
    static final String PATCHER_ID_PREFIX = "patcher_id_"

    String manifestPath

    ManifestTask() {
        group = PatcherPlugin.PATCHER_PLUGIN_GROUP
    }

    @TaskAction
    def updateManifest() {
        println(manifestPath)
    }
}

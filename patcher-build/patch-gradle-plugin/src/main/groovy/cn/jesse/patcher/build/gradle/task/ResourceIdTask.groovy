package cn.jesse.patcher.build.gradle.task

import cn.jesse.patcher.build.gradle.PatcherPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction;

/**
 * Created by jesse on 16/12/2016.
 */

public class ResourceIdTask extends DefaultTask {
    static final String RESOURCE_PUBLIC_XML = PatcherPlugin.PATCHER_INTERMEDIATES + "public.xml"
    static final String RESOURCE_IDX_XML = PatcherPlugin.PATCHER_INTERMEDIATES + "idx.xml"

    String resDir

    ResourceIdTask() {
        group = PatcherPlugin.PATCHER_PLUGIN_GROUP
    }

    @TaskAction
    def applyResourceId() {
        println(resDir)
    }
}

package cn.jesse.patcher.build.gradle.task

import cn.jesse.patcher.build.gradle.PatcherPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class PatcherTask extends DefaultTask {

    PatcherTask() {
        super()
        group = PatcherPlugin.PATCHER_PLUGIN_GROUP
    }

    @TaskAction
    def test() {
        println(project.patcher.toString());
        println(project.patcher.buildConfig.toString());
        println(project.patcher.dex.toString());
        println(project.patcher.lib.toString());
        println(project.patcher.res.toString());
        println(project.patcher.sevenZip.toString())
    }
}
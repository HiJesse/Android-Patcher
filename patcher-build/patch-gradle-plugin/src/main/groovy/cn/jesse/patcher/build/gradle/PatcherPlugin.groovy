package cn.jesse.patcher.build.gradle

import cn.jesse.patcher.build.gradle.extension.PatcherBuildConfigExtension
import cn.jesse.patcher.build.gradle.extension.PatcherDexExtension
import cn.jesse.patcher.build.gradle.extension.PatcherExtension
import cn.jesse.patcher.build.gradle.extension.PatcherLibExtension
import cn.jesse.patcher.build.gradle.extension.PatcherPackageConfigExtension
import cn.jesse.patcher.build.gradle.extension.PatcherResourceExtension
import cn.jesse.patcher.build.gradle.extension.PatcherSevenZipExtension
import cn.jesse.patcher.build.gradle.task.PatcherTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by jesse on 12/12/2016.
 */
public class PatcherPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('patcher', PatcherExtension)

        project.patcher.extensions.create('buildConfig', PatcherBuildConfigExtension)
        project.patcher.extensions.create('dex', PatcherDexExtension)
        project.patcher.extensions.create('lib', PatcherLibExtension)
        project.patcher.extensions.create('res', PatcherResourceExtension)
        project.patcher.extensions.create('packageConfig', PatcherPackageConfigExtension, project)
        project.patcher.extensions.create('sevenZip', PatcherSevenZipExtension, project)


        project.tasks.create("patcherTest", PatcherTask)
    }
}
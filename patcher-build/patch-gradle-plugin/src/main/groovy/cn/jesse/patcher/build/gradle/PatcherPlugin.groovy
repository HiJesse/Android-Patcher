package cn.jesse.patcher.build.gradle

import cn.jesse.patcher.build.gradle.extension.PatcherBuildConfigExtension
import cn.jesse.patcher.build.gradle.extension.PatcherDexExtension
import cn.jesse.patcher.build.gradle.extension.PatcherExtension
import cn.jesse.patcher.build.gradle.extension.PatcherLibExtension
import cn.jesse.patcher.build.gradle.extension.PatcherPackageConfigExtension
import cn.jesse.patcher.build.gradle.extension.PatcherResourceExtension
import cn.jesse.patcher.build.gradle.extension.PatcherSevenZipExtension
import cn.jesse.patcher.build.gradle.task.PatcherTask
import cn.jesse.patcher.build.util.FileOperation
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException

/**
 * Created by jesse on 12/12/2016.
 */
public class PatcherPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // 创建root扩展 patcher
        project.extensions.create('patcher', PatcherExtension)

        // 基于patcher再扩展出其他六项配置
        project.patcher.extensions.create('buildConfig', PatcherBuildConfigExtension)
        project.patcher.extensions.create('dex', PatcherDexExtension)
        project.patcher.extensions.create('lib', PatcherLibExtension)
        project.patcher.extensions.create('res', PatcherResourceExtension)
        project.patcher.extensions.create('packageConfig', PatcherPackageConfigExtension, project)
        project.patcher.extensions.create('sevenZip', PatcherSevenZipExtension, project)

        // 所有配置的引用
        def configuration = project.patcher

        // 如果不是application的话直接crash掉
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new GradleException('generatePatcherApk: Android Application plugin required')
        }

        // 拿到android扩展.
        def android = project.extensions.android

        //打包时去除注解的多余文件
        //add the patcher anno resource to the package exclude option
        android.packagingOptions.exclude("META-INF/services/javax.annotation.processing.Processor")
        android.packagingOptions.exclude("PatcherApplication.tmpl")

        //open jumboMode 默认打开jumboMode
        android.dexOptions.jumboMode = true

        // 可以配合incremental使用, 如果开启preDexLibraries
        //close preDexLibraries
        try {
            android.dexOptions.preDexLibraries = false
        } catch (Throwable e) {
            //no preDexLibraries field, just continue
        }

//        android.registerTransform(new AuxiliaryInjectTransform(project))


        // 修改声明 配属属性
        project.afterEvaluate() {
            project.logger.error("------------------------------------ patcher build warning ------------------------------------")
            project.logger.error("patcher auto operation: ")
            project.logger.error("excluding annotation processor and source template from app packaging.")
            project.logger.error("enable dx jumboMode to reduce package size.")
            project.logger.error("disable preDexLibraries to prevent ClassDefNotFoundException when your app is booting.")
            project.logger.error("")
            project.logger.error("patcher will change your build configs:")
            project.logger.error("we will add PATCHER_ID=${configuration.buildConfig.patcherId} in your build output manifest file build/intermediates/manifests/full/*")
            project.logger.error("")
            project.logger.error("if minifyEnabled is true")

            String tempMappingPath = configuration.buildConfig.applyMapping
            if (FileOperation.isLegalFile(tempMappingPath)) {
                project.logger.error("we will build ${project.getName()} apk with apply mapping file ${tempMappingPath}")
            }

//            project.logger.error("you will find the gen proguard rule file at ${TinkerProguardConfigTask.PROGUARD_CONFIG_PATH}")
            project.logger.error("and we will help you to put it in the proguardFiles.")
            project.logger.error("")
            project.logger.error("if multiDexEnabled is true")
//            project.logger.error("you will find the gen multiDexKeepProguard file at ${TinkerMultidexConfigTask.MULTIDEX_CONFIG_PATH}")
            project.logger.error("and you should copy it to your own multiDex keep proguard file yourself.")
            project.logger.error("")
            project.logger.error("if applyResourceMapping file is exist")
            String tempResourceMappingPath = configuration.buildConfig.applyResourceMapping
            if (FileOperation.isLegalFile(tempResourceMappingPath)) {
                project.logger.error("we will build ${project.getName()} apk with resource R.txt ${tempResourceMappingPath} file")
            } else {
                project.logger.error("we will build ${project.getName()} apk with resource R.txt file")
            }
            project.logger.error("if resources.arsc has changed, you should use applyResource mode to build the new apk!")
            project.logger.error("-----------------------------------------------------------------------------------------------")
        }

        // 遍历所有的variant
        android.applicationVariants.all { variant ->

            def variantOutput = variant.outputs.first()
            def variantName = variant.name.capitalize()

            // 禁止使用 instant run 避免对补丁生成产生影响
            try {
                def instantRunTask = project.tasks.getByName("transformClassesWithInstantRunFor${variantName}")
                if (instantRunTask) {
                    throw new GradleException(
                            "Tinker does not support instant run mode, please trigger build"
                                    + " by assemble${variantName} or disable instant run"
                                    + " in 'File->Settings...'."
                    )
                }
            } catch (UnknownTaskException e) {
                // Not in instant run mode, continue.
            }

        }


        project.tasks.create("patcherTest", PatcherTask)
    }
}
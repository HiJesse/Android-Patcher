package cn.jesse.patcher.build.gradle.task

import cn.jesse.patcher.build.gradle.PatcherPlugin
import cn.jesse.patcher.build.patch.InputParam
import cn.jesse.patcher.build.patch.Runner;
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

/**
 * Created by jesse on 15/12/2016.
 */

public class PatchSchemaTask extends DefaultTask {
    def configuration
    def android
    String buildApkPath
    String outputFolder
    def signConfig

    PatchSchemaTask() {
        super()
        description = 'Assemble Patcher Patch'
        group = PatcherPlugin.PATCHER_PLUGIN_GROUP
        outputs.upToDateWhen { false }
        configuration = project.patcher

        android = project.extensions.android
    }

    @TaskAction
    def patch() {
        println buildApkPath
        println outputFolder

        // 校验sourceApk是否有效
        configuration.checkParameter()
        // 校验patcherId是否有效
        configuration.buildConfig.checkParameter()
        // largeModSize不能小于0
        configuration.res.checkParameter()
        // dexMode只有两个模式 raw jar
        configuration.dex.checkDexMode()
        // 构建压缩环境
        configuration.sevenZip.resolveZipFinalPath()

        InputParam.Builder builder = new InputParam.Builder()
        if (configuration.useSign) {// 填充签名信息
            if (signConfig == null) {
                throw new GradleException("can't the get signConfig for ${taskName} build")
            }
            builder.setSignFile(signConfig.storeFile)
                    .setKeypass(signConfig.keyPassword)
                    .setStorealias(signConfig.keyAlias)
                    .setStorepass(signConfig.storePassword)

        }

        builder.setSourceApk(configuration.sourceApk)
                .setNewApk(buildApkPath)
                .setOutBuilder(outputFolder)
                .setIgnoreWarning(configuration.ignoreWarning)
                .setUsePreGeneratedPatchDex(configuration.dex.usePreGeneratedPatchDex)
                .setDexFilePattern(configuration.dex.pattern)
                .setDexLoaderPattern(configuration.dex.loader)
                .setDexMode(configuration.dex.dexMode)
                .setSoFilePattern(configuration.lib.pattern)
                .setResourceFilePattern(configuration.res.pattern)
                .setResourceIgnoreChangePattern(configuration.res.ignoreChange)
                .setResourceLargeModSize(configuration.res.largeModSize)
                .setUseApplyResource(configuration.buildConfig.usingResourceMapping)
                .setConfigFields(configuration.packageConfig.getFields())
                .setSevenZipPath(configuration.sevenZip.path)
                .setUseSign(configuration.useSign)

        InputParam inputParam = builder.create()
        Runner.gradleRun(inputParam);
    }
}

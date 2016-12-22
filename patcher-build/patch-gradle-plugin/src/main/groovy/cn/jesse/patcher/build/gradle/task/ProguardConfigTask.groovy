package cn.jesse.patcher.build.gradle.task

import cn.jesse.patcher.build.auxiliary.AuxiliaryClassInjector
import cn.jesse.patcher.build.gradle.PatcherPlugin
import cn.jesse.patcher.build.util.FileOperation;
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
                    "-dontwarn ${AuxiliaryClassInjector.NOT_EXISTS_CLASSNAME} \n" +
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
        def file = project.file(PROGUARD_CONFIG_PATH)
        project.logger.error("try update patcher proguard file with ${file}")

        // Create the directory if it doesnt exist already
        file.getParentFile().mkdirs()

        // Write our recommended proguard settings to this file
        FileWriter fr = new FileWriter(file.path)

        String applyMappingFile = project.patcher.buildConfig.applyMapping

        //write applymapping
        if (shouldApplyMapping && FileOperation.isLegalFile(applyMappingFile)) {
            // 将基准包的mapping文件apply进来
            project.logger.error("try add applymapping ${applyMappingFile} to build the package")
            fr.write("-applymapping " + applyMappingFile)
            fr.write("\n")
        } else {
            project.logger.error("applymapping file ${applyMappingFile} is illegal, just ignore")
        }

        // 默认的混淆写入文件
        fr.write(PROGUARD_CONFIG_SETTINGS)

        // 如果使用插桩模式, 则需要keep插桩涉及到的类和方法
        // Write additional rules to keep <init> and <clinit>
        if (project.patcher.dex.usePreGeneratedPatchDex) {
            def additionalKeptRules =
                    "-keep class ${AuxiliaryClassInjector.NOT_EXISTS_CLASSNAME} { \n" +
                            '    *; \n' +
                            '}\n' +
                            '\n' +
                            '-keepclassmembers class * { \n' +
                            '    <init>(...); \n' +
                            '    static void <clinit>(...); \n' +
                            '}\n'
            fr.write(additionalKeptRules)
            fr.write('\n')
        }

        // 将dex loader中配置的类keep
        fr.write("#your dex.loader patterns here\n")
        Iterable<String> loader = project.patcher.dex.loader
        for (String pattern : loader) {
            if (pattern.endsWith("*") && !pattern.endsWith("**")) {
                pattern += "*"
            }
            fr.write("-keep class " + pattern)
            fr.write("\n")
        }
        fr.close()

        // 将上面拼装起来的混淆文件添加文件列表中使其生效
        // Add this proguard settings file to the list
        applicationVariant.getBuildType().buildType.proguardFiles(file)
        def files = applicationVariant.getBuildType().buildType.getProguardFiles()
        project.logger.error("now proguard files is ${files}")
    }
}

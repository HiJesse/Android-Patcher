package cn.jesse.patcher.build.gradle.task

import cn.jesse.patcher.build.gradle.PatcherPlugin
import cn.jesse.patcher.build.util.FileOperation
import groovy.xml.Namespace;
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
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

        // 校验gradle中patcherId是否设置
        String patcherId = project.patcher.buildConfig.patcherId
        if (patcherId == null || patcherId.isEmpty()) {
            throw new GradleException('patcherId is not set!!!')
        }

        patcherId = PATCHER_ID_PREFIX + patcherId

        project.logger.error("patcher add ${patcherId} to your AndroidManifest.xml ${manifestPath}")

        def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")

        // 解析manifest文件
        def xml = new XmlParser().parse(new InputStreamReader(new FileInputStream(manifestPath), "utf-8"))
        def application = xml.application[0]
        if (application) {
            def metaDataTags = application['meta-data']

            // remove any old PATCHER_ID elements
            def existPatcherId = metaDataTags.findAll {
                it.attributes()[ns.name].equals(PATCHER_ID)
            }.each {
                it.parent().remove(it)
            }

            // Add the new PATCHER_ID element
            application.appendNode('meta-data', [(ns.name): PATCHER_ID, (ns.value): patcherId])

            // Write the manifest file
            def printer = new XmlNodePrinter(new PrintWriter(manifestPath, "utf-8"))
            printer.preserveWhitespace = true
            printer.print(xml)
        }

        // 拷贝修改过的manifest文件到patcher的中间编译路径下
        File manifestFile = new File(manifestPath)
        if (manifestFile.exists()) {
            FileOperation.copyFileUsingStream(manifestFile, project.file(MANIFEST_XML))
            project.logger.error("patcher gen AndroidManifest.xml in ${MANIFEST_XML}")
        }
    }
}

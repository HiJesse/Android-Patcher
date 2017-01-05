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
//        String resourceMappingFile = project.extensions.tinkerPatch.buildConfig.applyResourceMapping
//
//        // Parse the public.xml and ids.xml
//        if (!FileOperation.isLegalFile(resourceMappingFile)) {
//            project.logger.error("apply resource mapping file ${resourceMappingFile} is illegal, just ignore")
//            return
//        }
//
//        // 删除原有的ids 和public 映射文件
//        String idsXml = resDir + "/values/ids.xml";
//        String publicXml = resDir + "/values/public.xml";
//        FileOperation.deleteFile(idsXml);
//        FileOperation.deleteFile(publicXml);
//        List<String> resourceDirectoryList = new ArrayList<String>()
//        resourceDirectoryList.add(resDir)
//
//        // 将gradle中apply的r文件解析出来
//        project.logger.error("we build ${project.getName()} apk with apply resource mapping file ${resourceMappingFile}")
//        project.extensions.tinkerPatch.buildConfig.usingResourceMapping = true
//        Map<RDotTxtEntry.RType, Set<RDotTxtEntry>> rTypeResourceMap = PatchUtil.readRTxt(resourceMappingFile)
//
//        // 生成新的映射文件 并copy到intermediate路径下
//        AaptResourceCollector aaptResourceCollector = AaptUtil.collectResource(resourceDirectoryList, rTypeResourceMap)
//        PatchUtil.generatePublicResourceXml(aaptResourceCollector, idsXml, publicXml)
//        File publicFile = new File(publicXml)
//        if (publicFile.exists()) {
//            FileOperation.copyFileUsingStream(publicFile, project.file(RESOURCE_PUBLIC_XML))
//            project.logger.error("tinker gen resource public.xml in ${RESOURCE_PUBLIC_XML}")
//        }
//        File idxFile = new File(idsXml)
//        if (idxFile.exists()) {
//            FileOperation.copyFileUsingStream(idxFile, project.file(RESOURCE_IDX_XML))
//            project.logger.error("tinker gen resource idx.xml in ${RESOURCE_IDX_XML}")
//        }
    }
}

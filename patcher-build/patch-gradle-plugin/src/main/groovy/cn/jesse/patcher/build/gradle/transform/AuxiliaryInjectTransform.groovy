package cn.jesse.patcher.build.gradle.transform

import cn.jesse.patcher.build.auxiliary.AuxiliaryClassInjector
import cn.jesse.patcher.build.util.MD5
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.google.common.collect.ImmutableSet
import com.google.common.io.Files
import groovy.io.FileType
import groovy.xml.Namespace
import org.gradle.api.Project


/**
 * Created by jesse on 24/12/2016.
 */

public class AuxiliaryInjectTransform extends Transform {
    private static final String TRANSFORM_NAME = 'AuxiliaryInject'

    private final Project project

    private boolean isEnabled = false

    def applicationVariants

    /* ****** Variant related parameters start ****** */

    boolean isInitialized = false
    def appClassPathName = ''

    /* ******  Variant related parameters end  ****** */

    public AuxiliaryInjectTransform(Project project) {
        this.project = project

        project.afterEvaluate {
            this.isEnabled = project.patcher.dex.usePreGeneratedPatchDex
            this.applicationVariants = project.android.applicationVariants
        }
    }

    /**
     * Returns the unique name of the transform.
     *
     * <p/>
     * This is associated with the type of work that the transform does. It does not have to be
     * unique per variant.
     */
    @Override
    String getName() {
        return TRANSFORM_NAME
    }

    /**
     * Returns the type(s) of data that is consumed by the Transform. This may be more than
     * one type.
     *
     * <strong>This must be of type {@link QualifiedContent.DefaultContentType}</strong>
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return ImmutableSet.of(QualifiedContent.DefaultContentType.CLASSES)
    }

    /**
     * Returns the scope(s) of the Transform. This indicates which scopes the transform consumes.
     */
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return ImmutableSet.of(
                QualifiedContent.Scope.PROJECT,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
                QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES
        )
    }

    /**
     * Returns whether the Transform can perform incremental work.
     *
     * <p/>
     * If it does, then the TransformInput may contain a list of changed/removed/added files, unless
     * something else triggers a non incremental run.
     */
    @Override
    boolean isIncremental() {
        return true
    }

    /**
     * As same as TransformManager.getTaskNamePrefix
     */
    private String getTaskNamePrefix(Transform transform) {
        StringBuilder sb = new StringBuilder(100);
        sb.append("transform");

        Iterator<QualifiedContent.ContentType> iterator = transform.getInputTypes().iterator();
        // there's always at least one
        sb.append(iterator.next().name().toLowerCase(Locale.getDefault()).capitalize());
        while (iterator.hasNext()) {
            sb.append("And").append(
                    iterator.next().name().toLowerCase(Locale.getDefault()).capitalize());
        }

        sb.append("With").append(transform.getName().capitalize()).append("For");

        return sb.toString();
    }

    private String toLowerCase(String src) {
        char[] chars = src.toCharArray()
        chars[0] += (char) 32
        return new String(chars)
    }

    private void initVariantRelatedParamsIfNeeded(String variantName) {
        if (this.isInitialized) {
            return
        }

        def manifestFile = null
        def appClassName = ''

        // 根据当前variant获取到该variant的manifest文件, 这里使用闭包条件遍历
        this.applicationVariants.any { variant ->
            if (variant.name.equals(variantName)) {
                def variantOutput = variant.outputs.first()
                manifestFile = variantOutput.processManifest.manifestOutputFile
                return true  // break out.
            }
        }

        // 解析上面拿到的manifest文件,解析xml文件, 拿到application中android.name 编译过后的路径都为绝对路径
        if (manifestFile != null) {
            def parsedManifest = new XmlParser().parse(
                    new InputStreamReader(new FileInputStream(manifestFile), "utf-8"))
            def androidTag = new Namespace(
                    'http://schemas.android.com/apk/res/android', 'android')
            appClassName = parsedManifest.application[0].attribute(androidTag.name)

            if (appClassName != null && appClassName.length() > 0) {
                this.appClassPathName = appClassName.replace('.', '/') + '.class'
            }
        }

        this.isInitialized = true
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        printMsgLog("Inject is %b, incremental is %b", isEnabled, transformInvocation.incremental)

        // 获取完整的transform名字 e.g. sample:transformClassesWithAuxiliaryInjectForRelease
        // 再自己组装出部分transform名字 e.g. transformClassesWithAuxiliaryInjectFor
        // 将variant切割出来并转成小写 e.g. release
        initVariantRelatedParamsIfNeeded(toLowerCase(transformInvocation.context.path.split(getTaskNamePrefix(this))[1]))

        // 收集当前Transform的输入数据
        def dirInputs = new HashSet<>()
        def jarInputs = new HashSet<>()

        transformInvocation.inputs.each { input ->
            input.directoryInputs.each { dirInput ->
                dirInputs.add(dirInput)
            }
            input.jarInputs.each { jarInput ->
                jarInputs.add(jarInput)
            }
        }

        if (!this.isEnabled) {
            printMsgLog("PreGeneratedPatchDex mode is disabled, skip transforming.")
        }

        if (dirInputs.isEmpty() && jarInputs.isEmpty())
            return;

        // 创建当前Transform的处理classes dir output路径.
        // 当Format为DIRECTORY时是返回存放class文件的路径. Format为JAR,返回并创建出Jar文件.
        File dirOutput = transformInvocation.outputProvider.getContentLocation(
                "classes", getOutputTypes(), getScopes(), Format.DIRECTORY)
        if (!dirOutput.exists()) {
            dirOutput.mkdirs()
        }

        printMsgLog("Classes outputs " + dirOutput.absolutePath)

        if (!dirInputs.isEmpty()) {
            dirInputs.each { dirInput ->

                if (transformInvocation.isIncremental()) {
                    // 遍历开启了增量编译才有的changedFiles 返回Map<File, Status>
                    dirInput.changedFiles.each { entry ->
                        File fileInput = entry.getKey()
                        File fileOutput = new File(fileInput.getAbsolutePath().replace(
                                dirInput.file.getAbsolutePath(), dirOutput.getAbsolutePath()))
                        if (!fileOutput.exists()) {
                            fileOutput.getParentFile().mkdirs()
                        }
                        final String relativeInputClassPath =
                                dirInput.file.toPath().relativize(fileInput.toPath())
                                        .toString().replace('\\', '/')

                        // 获取到Status 根据不同的文件状态分别处理
                        Status fileStatus = entry.getValue()
                        switch(fileStatus) {
                            // ADDED CHANGED类型都需要保留input 并根据场景插桩
                            case Status.ADDED:
                            case Status.CHANGED:
                                if (fileInput.isDirectory()) {
                                    return // continue.
                                }

                                // If disabled or not a class file, skip transforming them.
                                if (!isEnabled || !fileInput.getName().endsWith('.class') || relativeInputClassPath.equals(this.appClassPathName)) {
                                    printWarnLog('Skipping class: %s', relativeInputClassPath)
                                    Files.copy(fileInput, fileOutput)
                                } else {
                                    printMsgLog('Processing %s file %s',
                                            fileStatus,
                                            relativeInputClassPath)
                                    AuxiliaryClassInjector.processClass(fileInput, fileOutput)
                                }
                                break
                            case Status.REMOVED:
                                // REMOVED类型直接把input删除掉
                                // Print log if it's enabled only.
                                if (this.isEnabled) {
                                    printMsgLog('Removing %s file %s from result.', fileStatus,
                                            dirOutput.toPath().relativize(fileOutput.toPath()).toString())
                                }

                                if (fileOutput.exists()) {
                                    if (fileOutput.isDirectory()) {
                                        fileOutput.deleteDir()
                                    } else {
                                        fileOutput.delete()
                                    }
                                }
                                break
                        }
                    }

                } else { // 没有开启增量编译 先清空输出路径

                    if (dirOutput.exists()) {
                        dirOutput.deleteDir()
                    }

                    // 使用traverse方法遍历获取到路径下的文件
                    dirInput.file.traverse(type: FileType.FILES) { fileInput ->
                        File fileOutput = new File(fileInput.getAbsolutePath().replace(dirInput.file.getAbsolutePath(), dirOutput.getAbsolutePath()))
                        if (!fileOutput.exists()) {
                            fileOutput.getParentFile().mkdirs()
                        }

                        final String relativeInputClassPath =
                                dirInput.file.toPath().relativize(fileInput.toPath())
                                        .toString().replace('\\', '/')

                        // 没有开启插桩或者不是class文件或者是application的class文件 则直接忽略
                        if (!isEnabled || !fileInput.getName().endsWith('.class') || relativeInputClassPath.equals(this.appClassPathName)) {
                            printMsgLog('Skipping file: %s', relativeInputClassPath)
                            Files.copy(fileInput, fileOutput)
                        } else {
                            printMsgLog('Processing %s file %s',
                                    Status.ADDED,
                                    relativeInputClassPath)
                            AuxiliaryClassInjector.processClass(fileInput, fileOutput)
                        }


                    }
                }
            }
        }

        // 遍历jar文件, 创建对应的输出jar文件, 插桩处理, 然后copy到对应的输出路径
        if (!jarInputs.isEmpty()) {
            // 储存起来出现过一次的class
            Map<String, String> entryNameToJarPathMap = new HashMap<>()

            jarInputs.each { jarInput ->
                File jarInputFile = jarInput.file
                // 这里需要注意第一个参数同DirOutput不同, DIRECTORY格式下是建立一个name路径, 而JAR格式是建立一个name.jar
                File jarOutputFile = transformInvocation.outputProvider.getContentLocation(
                        getUniqueHashName(jarInputFile), getOutputTypes(), getScopes(), Format.JAR)
                if (!jarOutputFile.exists()) {
                    jarOutputFile.getParentFile().mkdirs()
                }

                // 根据jar的不同状态分别处理
                switch (jarInput.status) {
                    case Status.NOTCHANGED:
                        // NOTCHANGED 和 增量编译同时出现时忽略该文件
                        //FIXME 直接break会不会有问题, 是否需要删除output文件
                        if (transformInvocation.incremental) {
                            break
                        }
                    case Status.ADDED:
                    case Status.CHANGED:
                        // Print log if it's enabled only.
                        if (this.isEnabled) {
                            printMsgLog('Processing %s jar file %s',
                                    transformInvocation.incremental ? jarInput.status : Status.ADDED,
                                    jarInputFile)
                        }

                        AuxiliaryClassInjector.processJar(jarInputFile, jarOutputFile, new AuxiliaryClassInjector.ProcessJarCallback() {
                            @Override
                            boolean onProcessClassEntry(String entryName) {
                                final String lastContainsJarPath = entryNameToJarPathMap.get(entryName)
                                if (lastContainsJarPath != null) {
                                    printWarnLog("Duplicate zip entry ${entryName} found in ${lastContainsJarPath} and ${jarInputFile.absolutePath}")
                                } else {
                                    entryNameToJarPathMap.put(entryName, jarInputFile.absolutePath)
                                }

                                // If disabled or not a class file, skip transforming them.
                                if (!this.isEnabled || !entryName.endsWith('.class')) {
                                    return false
                                } else {
                                    // Skip application class.
                                    if (entryName.equals(AuxiliaryInjectTransform.this.appClassPathName)) {
                                        return false
                                    } else {
                                        return true;
                                    }
                                }
                            }
                        })
                        break
                    case Status.REMOVED:
                        // REMOVED类型直接把input删除掉
                        // Print log if it's enabled only.
                        if (this.isEnabled) {
                            printMsgLog('Removing %s file %s from result.', fileStatus,
                                    jarOutputFile)
                        }

                        if (jarOutputFile.exists()) {
                            jarOutputFile.delete()
                        }
                        break
                }
            }
        }
    }

    /**
     * 如果fileInput为文件,则返回一个唯一的标识
     */
    private String getUniqueHashName(File fileInput) {
        final String fileInputName = fileInput.getName()
        if (fileInput.isDirectory()) {
            return fileInputName
        }
        final String parentDirPath = fileInput.getParentFile().getAbsolutePath()
        final String pathMD5 = MD5.getMessageDigest(parentDirPath.getBytes())
        final int extSepPos = fileInputName.lastIndexOf('.')
        final String fileInputNamePrefix =
                (extSepPos >= 0 ? fileInputName.substring(0, extSepPos) : fileInputName)
        return fileInputNamePrefix + '_' + pathMD5
    }

    private void printMsgLog(String fmt, Object... vals) {
        final String title = TRANSFORM_NAME.capitalize()
        this.project.logger.lifecycle("[{}] {}", title,
                (vals == null || vals.length == 0 ? fmt : String.format(fmt, vals)))
    }

    private void printWarnLog(String fmt, Object... vals) {
        final String title = TRANSFORM_NAME.capitalize()
        this.project.logger.error("[{}] {}", title,
                (vals == null || vals.length == 0 ? fmt : String.format(fmt, vals)))
    }
}

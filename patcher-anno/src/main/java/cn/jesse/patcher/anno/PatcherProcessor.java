package cn.jesse.patcher.anno;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by jesse on 14/11/2016.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class PatcherProcessor extends AbstractProcessor {
    private final String APPLICATION_TEMPLATE_PATH = "/PatcherApplication.tmpl";

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> supportedAnnotationTypes = new LinkedHashSet<>();

        supportedAnnotationTypes.add(PatcherApplication.class.getName());

        return supportedAnnotationTypes;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processPatcherApplication(roundEnv.getElementsAnnotatedWith(PatcherApplication.class));
        return true;
    }

    private void processPatcherApplication(Set<? extends Element> elements) {
        for (Element e : elements) {
            PatcherApplication ca = e.getAnnotation(PatcherApplication.class);

            //拿到代理类的名称和包名
            String proxyName = ((TypeElement) e).getQualifiedName().toString();
            String proxyPackageName = proxyName.substring(0, proxyName.lastIndexOf('.'));
            proxyName = proxyName.substring(proxyName.lastIndexOf('.') + 1);

            //拼装出真实的Application类名称
            String applicationClassName = ca.application();
            if (applicationClassName.startsWith(".")) {
                applicationClassName = proxyPackageName + applicationClassName;
            }

            //拆分出真实的Application类名称和包名
            String applicationPackageName = applicationClassName.substring(0, applicationClassName.lastIndexOf('.'));
            applicationClassName = applicationClassName.substring(applicationClassName.lastIndexOf('.') + 1);

            //拿到patch loader的名称
            String loaderClassName = ca.loaderClass();
            if (loaderClassName.startsWith(".")) {
                loaderClassName = proxyPackageName + loaderClassName;
            }

            //读取Application模板文件,将模板中的%KEY%占位符全部替换成真实的数据
            final InputStream is = PatcherProcessor.class.getResourceAsStream(APPLICATION_TEMPLATE_PATH);
            final Scanner scanner = new Scanner(is);
            final String template = scanner.useDelimiter("\\A").next();
            final String fileContent = template
                    .replaceAll("%PACKAGE%", applicationPackageName)
                    .replaceAll("%APPLICATION%", applicationClassName)
                    .replaceAll("%APPLICATION_PROXY%", proxyPackageName + "." + proxyName)
                    .replaceAll("%PATCH_FLAGS%", "" + ca.flags())
                    .replaceAll("%PATCH_LOADER_CLASS%", "" + loaderClassName)
                    .replaceAll("%PATCH_LOAD_VERIFY_FLAG%", "" + ca.loadVerifyFlag());

            //将完整的Application代码写入到跟代理Application的相同路径下的文件中
            // 至此注解生成真实Application的工作就完成了
            try {
                JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(applicationPackageName + "." + applicationClassName);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Creating " + fileObject.toUri());
                Writer writer = fileObject.openWriter();
                try {
                    PrintWriter pw = new PrintWriter(writer);
                    pw.print(fileContent);
                    pw.flush();

                } finally {
                    writer.close();
                }
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, x.toString());
            }
        }
    }
}

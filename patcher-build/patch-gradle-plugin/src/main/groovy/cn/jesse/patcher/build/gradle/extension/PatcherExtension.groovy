package cn.jesse.patcher.build.gradle.extension

import org.gradle.api.GradleException;

/**
 * Created by jesse on 12/12/2016.
 */
public class PatcherExtension {

    /**
     * Specifies the old apk path to diff with the new apk
     */
    String sourceApk

    /**
     * If there is loader class changes,
     * or Activity, Service, Receiver, Provider change, it will terminal
     * if ignoreWarning is false
     * default: false
     */
    boolean ignoreWarning

    /**
     * If sign the patch file with the android signConfig
     * default: true
     */
    boolean useSign



    PatcherExtension() {
        sourceApk = ''
        ignoreWarning = false
        useSign = true
    }

    /**
     * 校验sourceApk有效, 并且文件存在
     */
    void checkParameter() {
        if (sourceApk == null) {
            throw new GradleException("source apk is null, you must set the correct old apk value!")
        }
        File apk = new File(sourceApk)
        if (!apk.exists()) {
            throw new GradleException("source apk ${sourceApk} is not exist, you must set the correct source apk value!")
        } else if (!apk.isFile()) {
            throw new GradleException("source apk ${sourceApk} is a directory, you must set the correct source apk value!")
        }

    }

    @Override
    public String toString() {
        """| sourceApk = ${sourceApk}
           | ignoreWarning = ${ignoreWarning}
           | useSign = ${useSign}
        """.stripMargin()
    }
}

package cn.jesse.patcher.build.gradle.extension

import org.gradle.api.GradleException

/**
 * Created by jesse on 13/12/2016.
 */

public class PatcherBuildConfigExtension {
    /**
     * Specifies the source apk's mapping file for proguard to applymapping
     */
    String applyMapping

    /**
     * Specifies the source resource id mapping(R.txt) file to applyResourceMapping
     */
    String applyResourceMapping

    /**
     * because we don't want to check the base apk with md5 in the runtime(it is slow)
     * patcherId is use to identify the unique base apk when the patch is tried to apply.
     * we can use git rev, svn rev or simply versionCode.
     * we will gen the patcherId in your manifest automatic
     */
    String patcherId

    boolean usingResourceMapping

    public PatcherBuildConfigExtension() {
        applyMapping = ""
        applyResourceMapping = ""
        patcherId = null
        usingResourceMapping = false
    }

    void checkParameter() {
        if (patcherId == null || patcherId.isEmpty()) {
            throw new GradleException("you must set your patcherId to identify the base apk!")
        }
    }


    @Override
    public String toString() {
        """| applyMapping = ${applyMapping}
           | applyResourceMapping = ${applyResourceMapping}
           | patcherId = ${patcherId}
        """.stripMargin()
    }
}

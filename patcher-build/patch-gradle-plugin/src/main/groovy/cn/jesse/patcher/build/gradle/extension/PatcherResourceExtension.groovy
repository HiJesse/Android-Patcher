package cn.jesse.patcher.build.gradle.extension

import org.gradle.api.GradleException;

/**
 * Created by jesse on 13/12/2016.
 */

public class PatcherResourceExtension {
    /**
     * the resource file patterns, which files will be deal to gen patch
     * such as [res/*, assets/*, resources.arsc]
     */
    Iterable<String> pattern
    /**
     * the resource file ignoreChange patterns, ignore add, delete or modify resource change
     * Warning, we can only use for files no relative with resources.arsc
     */
    Iterable<String> ignoreChange

    /**
     * default 100kb
     * for modify resource, if it is larger than 'largeModSize'
     * we would like to use bsdiff algorithm to reduce patch file size
     */
    int largeModSize

    public PatcherResourceExtension() {
        pattern = []
        ignoreChange = []
        largeModSize = 100
    }

    void checkParameter() {
        if (largeModSize <= 0) {
            throw new GradleException("largeModSize must be larger than 0")
        }
    }

    @Override
    public String toString() {
        """| pattern = ${pattern}
           | exclude = ${ignoreChange}
           | largeModSize = ${largeModSize}kb
        """.stripMargin()
    }
}

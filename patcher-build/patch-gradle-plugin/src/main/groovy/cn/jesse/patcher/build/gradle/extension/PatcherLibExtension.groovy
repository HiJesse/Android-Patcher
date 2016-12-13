package cn.jesse.patcher.build.gradle.extension;

/**
 * Created by jesse on 13/12/2016.
 */

public class PatcherLibExtension {
    /**
     * the library file patterns, which files will be deal to gen patch
     * such as [lib/armeabi/*.so, assets/libs/*.so]
     */
    Iterable<String> pattern;


    public PatcherLibExtension() {
        pattern = []
    }

    @Override
    public String toString() {
        """| pattern = ${pattern}
        """.stripMargin()
    }
}

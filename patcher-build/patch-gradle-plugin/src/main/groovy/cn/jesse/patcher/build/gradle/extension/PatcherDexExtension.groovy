package cn.jesse.patcher.build.gradle.extension;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

/**
 * Created by jesse on 13/12/2016.
 */

public class PatcherDexExtension {
    /**
     * raw or jar, if you want to support below 4.0, you should use jar
     * default: raw, keep the orginal file type
     */
    String dexMode;

    /**
     * If mUsePreGeneratedPatchDex was enabled, patcher framework would generate
     * a dex file including all added and changed classes instead of patch info file.
     *
     * You can make this mode enabled if you're using any dex encrypting solutions or
     * maintaining patches that suitable for multi-channel base packages.
     *
     * Notice that although you use this mode, proguard mappings should still be applied
     * to base package and all patched packages.
     */
    boolean usePreGeneratedPatchDex

    /**
     * the dex file patterns, which dex or jar files will be deal to gen patch
     * such as [classes.dex, classes-*.dex, assets/multiDex/*.jar]
     */
    Iterable<String> pattern;
    /**
     * the loader files, they will be removed during gen patch main dex
     * and they should be at the primary dex
     * such as [cn.jesse.patcher.loader.*]
     */
    Iterable<String> loader;

    public PatcherDexExtension() {
        dexMode = "jar"
        usePreGeneratedPatchDex = false
        pattern = []
        loader = []
    }

    /**
     * dexMode 是有两个模式 raw jar
     */
    void checkDexMode() {
        if (!dexMode.equals("raw") && !dexMode.equals("jar")) {
            throw new GradleException("dexMode can be only one of 'jar' or 'raw'!")
        }
    }

    @Override
    public String toString() {
        """| dexMode = ${dexMode}
                | usePreGeneratedPatchDex = ${usePreGeneratedPatchDex}
        | pattern = ${pattern}
        | loader = ${loader}
        """.stripMargin()
    }
}

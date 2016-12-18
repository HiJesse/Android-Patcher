package cn.jesse.patcher.build.patch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jesse on 18/12/2016.
 */

public class InputParam {
    /**
     * patch env
     */
    public final String  sourceApk;
    public final String  newApk;
    public final String  outFolder;
    public final File signFile;
    public final String  keypass;
    public final String  storealias;
    public final String  storepass;
    public final boolean ignoreWarning;
    public final boolean usePreGeneratedPatchDex;
    public final boolean useSign;

    /**
     * patch.dex
     */
    public final ArrayList<String>       dexFilePattern;
    public final ArrayList<String>       dexLoaderPattern;
    public final String                  dexMode;
    /**
     * patch.lib
     */
    public final ArrayList<String>       soFilePattern;
    /**
     * patch.resource pattern
     */
    public final ArrayList<String>       resourceFilePattern;
    /**
     * path.resource ignoreChange
     */
    public final ArrayList<String>       resourceIgnoreChangePattern;
    /**
     * path.resource largeModSize
     */
    public final int                     largeModSize;
    /**
     * path.buildConfig applyResourceMapping
     */
    public final boolean                 useApplyResource;
    /**
     * patch.packageConfig
     */
    public final HashMap<String, String> configFields;
    /**
     * patch.sevenZip
     */
    public final String                  sevenZipPath;

    private InputParam(
            String sourceApk,
            String newApk,
            String outFolder,
            File signFile,
            String keypass,
            String storealias,
            String storepass,
            boolean ignoreWarning,
            boolean usePreGeneratedPatchDex,
            boolean useSign,

            ArrayList<String> dexFilePattern,
            ArrayList<String> dexLoaderPattern,
            String dexMode,
            ArrayList<String> soFilePattern,
            ArrayList<String> resourceFilePattern,
            ArrayList<String> resourceIgnoreChangePattern,
            int largeModSize,
            boolean useApplyResource,
            HashMap<String, String> configFields,

            String sevenZipPath
    ) {
        this.sourceApk = sourceApk;
        this.newApk = newApk;
        this.outFolder = outFolder;
        this.signFile = signFile;
        this.keypass = keypass;
        this.storealias = storealias;
        this.storepass = storepass;
        this.ignoreWarning = ignoreWarning;
        this.usePreGeneratedPatchDex = usePreGeneratedPatchDex;
        this.useSign = useSign;

        this.dexFilePattern = dexFilePattern;
        this.dexLoaderPattern = dexLoaderPattern;
        this.dexMode = dexMode;

        this.soFilePattern = soFilePattern;
        this.resourceFilePattern = resourceFilePattern;
        this.resourceIgnoreChangePattern = resourceIgnoreChangePattern;
        this.largeModSize = largeModSize;
        this.useApplyResource = useApplyResource;

        this.configFields = configFields;

        this.sevenZipPath = sevenZipPath;
    }

    public static class Builder {
        /**
         * patch
         */
        private String  sourceApk;
        private String  newApk;
        private String  outFolder;
        private File    signFile;
        private String  keypass;
        private String  storealias;
        private String  storepass;
        private boolean ignoreWarning;
        private boolean usePreGeneratedPatchDex;
        private boolean useSign;

        /**
         * patch.dex
         */
        private ArrayList<String>       dexFilePattern;
        private ArrayList<String>       dexLoaderPattern;
        private String                  dexMode;
        /**
         * patch.lib
         */
        private ArrayList<String>       soFilePattern;
        /**
         * path.resource pattern
         */
        private ArrayList<String>       resourceFilePattern;
        /**
         * path.resource ignoreChange
         */
        private ArrayList<String>       resourceIgnoreChangePattern;
        /**
         * path.resource largeModSize
         */
        private  int                    largeModSize;
        /**
         * path.buildConfig applyResourceMapping
         */
        private boolean                 useApplyResource;
        /**
         * patch.packageConfig
         */
        private HashMap<String, String> configFields;
        /**
         * patch.sevenZip
         */
        private String                  sevenZipPath;


        public Builder() {
        }

        public Builder setSourceApk(String sourceApk) {
            this.sourceApk = sourceApk;
            return this;
        }

        public Builder setNewApk(String newApk) {
            this.newApk = newApk;
            return this;
        }

        public Builder setSoFilePattern(ArrayList<String> soFilePattern) {
            this.soFilePattern = soFilePattern;
            return this;
        }

        public Builder setResourceFilePattern(ArrayList<String> resourceFilePattern) {
            this.resourceFilePattern = resourceFilePattern;
            return this;
        }

        public Builder setResourceIgnoreChangePattern(ArrayList<String> resourceIgnoreChangePattern) {
            this.resourceIgnoreChangePattern = resourceIgnoreChangePattern;
            return this;
        }

        public Builder setResourceLargeModSize(int largeModSize) {
            this.largeModSize = largeModSize;
            return this;
        }

        public Builder setUseApplyResource(boolean useApplyResource) {
            this.useApplyResource = useApplyResource;
            return this;
        }

        public Builder setDexFilePattern(ArrayList<String> dexFilePattern) {
            this.dexFilePattern = dexFilePattern;
            return this;
        }

        public Builder setOutBuilder(String outFolder) {
            this.outFolder = outFolder;
            return this;
        }

        public Builder setSignFile(File signFile) {
            this.signFile = signFile;
            return this;
        }

        public Builder setKeypass(String keypass) {
            this.keypass = keypass;
            return this;
        }

        public Builder setStorealias(String storealias) {
            this.storealias = storealias;
            return this;
        }

        public Builder setStorepass(String storepass) {
            this.storepass = storepass;
            return this;
        }

        public Builder setIgnoreWarning(boolean ignoreWarning) {
            this.ignoreWarning = ignoreWarning;
            return this;
        }

        public Builder setUsePreGeneratedPatchDex(boolean usePreGeneratedPatchDex) {
            this.usePreGeneratedPatchDex = usePreGeneratedPatchDex;
            return this;
        }

        public Builder setDexLoaderPattern(ArrayList<String> dexLoaderPattern) {
            this.dexLoaderPattern = dexLoaderPattern;
            return this;
        }

        public Builder setDexMode(String dexMode) {
            this.dexMode = dexMode;
            return this;
        }

        public Builder setConfigFields(HashMap<String, String> configFields) {
            this.configFields = configFields;
            return this;
        }

        public Builder setSevenZipPath(String sevenZipPath) {
            this.sevenZipPath = sevenZipPath;
            return this;
        }

        public Builder setUseSign(boolean useSign) {
            this.useSign = useSign;
            return this;
        }

        public InputParam create() {
            return new InputParam(
                    sourceApk,
                    newApk,
                    outFolder,
                    signFile,
                    keypass,
                    storealias,
                    storepass,
                    ignoreWarning,
                    usePreGeneratedPatchDex,
                    useSign,
                    dexFilePattern,
                    dexLoaderPattern,
                    dexMode,
                    soFilePattern,
                    resourceFilePattern,
                    resourceIgnoreChangePattern,
                    largeModSize,
                    useApplyResource,
                    configFields,
                    sevenZipPath
            );
        }
    }
}
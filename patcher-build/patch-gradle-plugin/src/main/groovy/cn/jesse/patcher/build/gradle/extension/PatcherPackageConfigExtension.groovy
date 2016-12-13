package cn.jesse.patcher.build.gradle.extension;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.io.File;
import java.util.Map;

/**
 * Created by jesse on 13/12/2016.
 */

public class PatcherPackageConfigExtension {
    /**
     * we can gen package config file while configField method
     */
    private Map<String, String> fields
    private Project project;
//    private AndroidParser androidManifest;


    public PatcherPackageConfigExtension(project) {
        fields = [:]
        this.project = project
    }

    void configField(String name, String value) {
        fields.put(name, value)
    }

    Map<String, String> getFields() {
        return fields
    }

//    private void createApkMetaFile() {
//        if (androidManifest == null) {
//            File sourceApk = new File(project.patcher.sourceApk)
//
//            if (!sourceApk.exists()) {
//                throw new GradleException(
//                        String.format("source apk file %s is not exist, you can set the value directly!", sourceApk)
//                )
//            }
//            androidManifest = AndroidParser.getAndroidManifest(sourceApk);
//        }
//    }
//
//    String getVersionCodeFromSourceAPk() {
//        createApkMetaFile()
//        return androidManifest.apkMeta.versionCode;
//    }
//
//    String getVersionNameFromSourceAPk() {
//        createApkMetaFile()
//        return androidManifest.apkMeta.versionName;
//    }
//
//    String getMinSdkVersionFromSourceAPk() {
//        createApkMetaFile()
//        return androidManifest.apkMeta.minSdkVersion;
//    }
//
//    String getMetaDataFromSourceApk(String name) {
//        createApkMetaFile()
//        String value = androidManifest.metaDatas.get(name);
//        if (value == null) {
//            throw new GradleException("can't find meta data " + name + " from the Source apk manifest file!")
//        }
//        return value
//    }

    @Override
    public String toString() {
        """| fields = ${fields}
        """.stripMargin()
    }
}

package cn.jesse.patcher.build.gradle.extension;

import org.gradle.api.Project;

/**
 * Created by jesse on 13/12/2016.
 */

public class PatcherPackageConfigExtension {
    /**
     * we can gen package config file while configField method
     */
    private Map<String, String> fields
    private Project project;


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

    @Override
    public String toString() {
        """| fields = ${fields}
        """.stripMargin()
    }
}

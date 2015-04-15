package com.oneandone.go.plugin.maven.message;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ConfigurationMessage {

    @Expose
    @SerializedName("repository-configuration")
    private Map<String, PackageMaterialProperty> repositoryConfiguration;

    @Expose
    @SerializedName("package-configuration")
    private Map<String, PackageMaterialProperty> packageConfiguration;

    public PackageMaterialProperties getRepositoryConfiguration() {
        return new PackageMaterialProperties(repositoryConfiguration);
    }

    public PackageMaterialProperties getPackageConfiguration() {
        return new PackageMaterialProperties(packageConfiguration);
    }
}

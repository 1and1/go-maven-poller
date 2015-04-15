package com.oneandone.go.plugin.maven.message;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.Map;

public class LatestPackageRevisionMessage {

    @Expose
    @SerializedName("repository-configuration")
    private Map<String, PackageMaterialProperty> repositoryConfiguration;

    @Expose
    @SerializedName("package-configuration")
    private Map<String, PackageMaterialProperty> packageConfiguration;

    @Expose
    @SerializedName("previous-revision")
    @Getter private PackageRevisionMessage previousRevision;

    public PackageMaterialProperties getRepositoryConfiguration() {
        return new PackageMaterialProperties(repositoryConfiguration);
    }

    public PackageMaterialProperties getPackageConfiguration() {
        return new PackageMaterialProperties(packageConfiguration);
    }
}

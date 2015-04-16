package com.oneandone.go.plugin.maven.message;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.Map;

/** The configuration message as send by the Go CD server to supply all configuration properties. */
public class ConfigurationMessage {

    /** The repository configuration property map. */
    @Expose
    @SerializedName("repository-configuration")
    private Map<String, PackageMaterialProperty> repositoryConfiguration;

    /** The package configuration property map. */
    @Expose
    @SerializedName("package-configuration")
    private Map<String, PackageMaterialProperty> packageConfiguration;

    /**
     * The last known revision.
     *
     * @return the last known revision
     */
    @Expose
    @SerializedName("previous-revision")
    @Getter private PackageRevisionMessage previousRevision;

    /**
     * Returns the repository configuration properties.
     *
     * @return the repository configuration properties
     */
    public PackageMaterialProperties getRepositoryConfiguration() {
        return new PackageMaterialProperties(repositoryConfiguration);
    }

    /**
     * Returns the package configuration properties.
     *
     * @return the package configuration properties
     */
    public PackageMaterialProperties getPackageConfiguration() {
        return new PackageMaterialProperties(packageConfiguration);
    }
}

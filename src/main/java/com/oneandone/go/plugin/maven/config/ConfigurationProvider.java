package com.oneandone.go.plugin.maven.config;

import com.oneandone.go.plugin.maven.message.PackageMaterialProperties;
import com.oneandone.go.plugin.maven.message.ValidationResultMessage;

import static com.oneandone.go.plugin.maven.config.ConfigurationProperties.*;

/** The configuration definition provider for this plugin. */
public class ConfigurationProvider {

    /**
     * Returns the configuration definition for the repository configuration.
     *
     * @return the configuration definition for the repository configuration
     */
    public PackageMaterialProperties getRepositoryConfiguration() {
        final PackageMaterialProperties repoConfig = new PackageMaterialProperties();
        repoConfig.addPackageMaterialProperty(REPOSITORY_CONFIGURATION_KEY_REPO_URL, getRepositoryConfigurationPropertyRepoUrl());
        repoConfig.addPackageMaterialProperty(REPOSITORY_CONFIGURATION_KEY_USERNAME, getRepositoryConfigurationPropertyUsername());
        repoConfig.addPackageMaterialProperty(REPOSITORY_CONFIGURATION_KEY_PASSWORD, getRepositoryConfigurationPropertyPassword());
        repoConfig.addPackageMaterialProperty(REPOSITORY_CONFIGURATION_KEY_PROXY, getRepositoryConfigurationPropertyProxy());
        return repoConfig;
    }

    /**
     * Returns the configuration definition for the package configuration.
     *
     * @return the configuration definition for the package configuration
     */
    public PackageMaterialProperties getPackageConfiguration() {
        final PackageMaterialProperties packageConfig = new PackageMaterialProperties();
        packageConfig.addPackageMaterialProperty(PACKAGE_CONFIGURATION_KEY_GROUP_ID, getPackageConfigurationPropertyGroupId());
        packageConfig.addPackageMaterialProperty(PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID, getPackageConfigurationPropertyArtifactId());
        packageConfig.addPackageMaterialProperty(PACKAGE_CONFIGURATION_KEY_PACKAGING, getPackageConfigurationPropertyPackaging());
        packageConfig.addPackageMaterialProperty(PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM, getPackageConfigurationPropertyPollVersionFrom());
        packageConfig.addPackageMaterialProperty(PACKAGE_CONFIGURATION_KEY_POLL_VERSION_TO, getPackageConfigurationPropertyPollVersionTo());
        return  packageConfig;
    }

    /**
     * Validates the repository configuration.
     *
     * @param repoConfig the repository configuration to validate
     * @return the validation result
     * @see MavenRepoConfig#validate()
     */
    public ValidationResultMessage isRepositoryConfigurationValid(final PackageMaterialProperties repoConfig) {
        return new MavenRepoConfig(repoConfig).validate();
    }

    /**
     * Validates the package configuration.
     *
     * @param packageConfig the package configuration to validate
     * @return the validation result
     * @see MavenPackageConfig#validate()
     */
    public ValidationResultMessage isPackageConfigurationValid(final PackageMaterialProperties packageConfig) {
        return new MavenPackageConfig(packageConfig, null).validate();
    }
}

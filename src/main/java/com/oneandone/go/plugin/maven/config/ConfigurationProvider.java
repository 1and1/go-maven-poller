package com.oneandone.go.plugin.maven.config;

import com.oneandone.go.plugin.maven.message.PackageMaterialProperties;
import com.oneandone.go.plugin.maven.message.ValidationResultMessage;

import static com.oneandone.go.plugin.maven.config.ConfigurationProperties.*;

public class ConfigurationProvider {

    public PackageMaterialProperties getRepositoryConfiguration() {
        final PackageMaterialProperties repoConfig = new PackageMaterialProperties();
        repoConfig.addPackageMaterialProperty(REPOSITORY_CONFIGURATION_KEY_REPO_URL, getRepositoryConfigurationPropertyRepoUrl());
        repoConfig.addPackageMaterialProperty(REPOSITORY_CONFIGURATION_KEY_USERNAME, getRepositoryConfigurationPropertyUsername());
        repoConfig.addPackageMaterialProperty(REPOSITORY_CONFIGURATION_KEY_PASSWORD, getRepositoryConfigurationPropertyPassword());
        repoConfig.addPackageMaterialProperty(REPOSITORY_CONFIGURATION_KEY_PROXY, getRepositoryConfigurationPropertyProxy());
        return repoConfig;
    }

    public PackageMaterialProperties getPackageConfiguration() {
        final PackageMaterialProperties packageConfig = new PackageMaterialProperties();
        packageConfig.addPackageMaterialProperty(PACKAGE_CONFIGURATION_KEY_GROUP_ID, getPackageConfigurationPropertyGroupId());
        packageConfig.addPackageMaterialProperty(PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID, getPackageConfigurationPropertyArtifactId());
        packageConfig.addPackageMaterialProperty(PACKAGE_CONFIGURATION_KEY_PACKAGING, getPackageConfigurationPropertyPackaging());
        packageConfig.addPackageMaterialProperty(PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM, getPackageConfigurationPropertyPollVersionFrom());
        packageConfig.addPackageMaterialProperty(PACKAGE_CONFIGURATION_KEY_POLL_VERSION_TO, getPackageConfigurationPropertyPollVersionTo());
        return  packageConfig;
    }

    public ValidationResultMessage isRepositoryConfigurationValid(final PackageMaterialProperties repoConfig) {
        return new MavenRepoConfig(repoConfig).validate();
    }

    public ValidationResultMessage isPackageConfigurationValid(final PackageMaterialProperties packageConfig) {
        return new MavenPackageConfig(packageConfig, null).validate();
    }
}

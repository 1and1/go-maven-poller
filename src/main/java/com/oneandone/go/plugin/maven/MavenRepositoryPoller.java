package com.oneandone.go.plugin.maven;

import com.oneandone.go.plugin.maven.client.RepositoryClient;
import com.oneandone.go.plugin.maven.client.RepositoryConnector;
import com.oneandone.go.plugin.maven.config.ConfigurationProperties;
import com.oneandone.go.plugin.maven.config.ConfigurationProvider;
import com.oneandone.go.plugin.maven.config.MavenPackageConfig;
import com.oneandone.go.plugin.maven.config.MavenRepoConfig;
import com.oneandone.go.plugin.maven.message.*;
import com.oneandone.go.plugin.maven.util.MavenRevision;
import com.thoughtworks.go.plugin.api.logging.Logger;


public class MavenRepositoryPoller {

    private static Logger LOGGER = Logger.getLoggerFor(MavenRepositoryPoller.class);

    public PackageRevisionMessage getLatestRevision(final PackageMaterialProperties packageConfig, final PackageMaterialProperties repoConfig) {
        LOGGER.info(String.format("getLatestRevision called with groupId %s, artifactId %s, for repo: %s",
                        packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_GROUP_ID).orNull(),
                        packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID).orNull(),
                        repoConfig.getValue(ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_REPO_URL).orNull())
        );
        validateConfig(repoConfig, packageConfig);
        final PackageRevisionMessage packageRevision = poll(new MavenRepoConfig(repoConfig), new MavenPackageConfig(packageConfig, null));
        if (packageRevision != null) {
            LOGGER.info(String.format("getLatestRevision returning with %s, %s", packageRevision.getRevision(), packageRevision.getTimestamp()));
        }
        return packageRevision;
    }

    public PackageRevisionMessage latestModificationSince(final PackageMaterialProperties packageConfig, final PackageMaterialProperties repoConfig, final PackageRevisionMessage previouslyKnownRevision) {
        LOGGER.info(String.format("latestModificationSince called with groupId %s, for repo: %s",
                        packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_GROUP_ID).orNull(),
                        repoConfig.getValue(ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_REPO_URL).orNull())
        );
        validateConfig(repoConfig, packageConfig);
        final PackageRevisionMessage updatedPackage = poll(new MavenRepoConfig(repoConfig), new MavenPackageConfig(packageConfig, previouslyKnownRevision));
        if (updatedPackage == null) {
            LOGGER.info(String.format("no modification since %s", previouslyKnownRevision.getRevision()));
            return null;
        }
        LOGGER.info(String.format("latestModificationSince returning with %s, %s", updatedPackage.getRevision(), updatedPackage.getTimestamp()));
        if (updatedPackage.getTimestamp().getTime() < previouslyKnownRevision.getTimestamp().getTime())
            LOGGER.warn(String.format("Updated Package %s published earlier (%s) than previous (%s, %s)",
                            updatedPackage.getRevision(),
                            updatedPackage.getTimestamp(),
                            previouslyKnownRevision.getRevision(),
                            previouslyKnownRevision.getTimestamp())
            );
        return updatedPackage;
    }

    public CheckConnectionResultMessage checkConnectionToRepository(final PackageMaterialProperties repoConfig) {
        try {
            final MavenRepoConfig mavenRepoConfig = new MavenRepoConfig(repoConfig);
            if (!new RepositoryConnector(mavenRepoConfig).testConnection()) {
                return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, "Did not get HTTP Status 200 response");
            }
        } catch (final Exception e) {
            return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, e.getMessage());
        }
        return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.SUCCESS);
    }

    public CheckConnectionResultMessage checkConnectionToPackage(PackageMaterialProperties packageConfig, PackageMaterialProperties repoConfig) {
        final CheckConnectionResultMessage repoCheckResult = checkConnectionToRepository(repoConfig);
        if (!repoCheckResult.success()) {
            return repoCheckResult;
        }
        final PackageRevisionMessage packageRevision = getLatestRevision(packageConfig, repoConfig);

        if (packageRevision != null && packageRevision.getRevision() != null && packageRevision.getDataFor("LOCATION") != null) {
            return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.SUCCESS, "Found " + packageRevision.getRevision());
        } else {
            return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, "Could not find package");
        }
    }

    private void validateConfig(final PackageMaterialProperties repoConfig, final PackageMaterialProperties packageConfig) {
        final ValidationResultMessage validationResult = new ConfigurationProvider().isRepositoryConfigurationValid(repoConfig);
        validationResult.addErrors(new ConfigurationProvider().isPackageConfigurationValid(packageConfig).getValidationErrors());

        if (!validationResult.success()) {
            final StringBuilder stringBuilder = new StringBuilder();
            for (final ValidationError validationError : validationResult.getValidationErrors()) {
                stringBuilder.append(validationError.getMessage()).append("; ");
            }
            final String errorString = stringBuilder.toString();
            throw new RuntimeException(errorString.substring(0, errorString.length() - 2));
        }
    }

    private PackageRevisionMessage poll(final MavenRepoConfig repoConfig, final MavenPackageConfig packageConfig) {
        final MavenRevision latest = new RepositoryClient(repoConfig, packageConfig).getLatest();
        if (latest == null) {
            return new PackageRevisionMessage();
        }
        return latest.toPackageRevision();
    }
}

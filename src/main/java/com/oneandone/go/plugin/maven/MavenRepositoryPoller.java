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

/**
 * This class handles all maven repository requests and delegates them accordingly.
 * <p />
 * The implementations for the actual repository connections are packaged in {@link com.oneandone.go.plugin.maven.client}.
 */
public class MavenRepositoryPoller {

    /** The logging instance for this class. */
    private static final Logger LOGGER = Logger.getLoggerFor(MavenRepositoryPoller.class);

    /**
     * Returns the latest package revision of the artifact specified in the package configuration within the specified repository.
     * <p />
     * If a revision could not be found for the specified package criteria, this method will return {@code null}.
     *
     * @param packageConfig the package configuration (see {@link MavenPackageConfig})
     * @param repoConfig the repository configuration (see {@link MavenRepoConfig})
     * @return the latest package revision or {@code null}
     * @throws RuntimeException if the specified configuration could not be validated successfully
     */
    public PackageRevisionMessage getLatestRevision(final PackageMaterialProperties packageConfig, final PackageMaterialProperties repoConfig) {
        LOGGER.info(String.format("check of latest for artifact with groupId: '%s', artifactId: '%s' in repo: %s",
                        packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_GROUP_ID).orNull(),
                        packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID).orNull(),
                        repoConfig.getValue(ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_REPO_URL).orNull())
        );
        validateConfig(repoConfig, packageConfig);
        final PackageRevisionMessage packageRevision = poll(new MavenRepoConfig(repoConfig), new MavenPackageConfig(packageConfig, null));
        if (packageRevision != null) {
            LOGGER.info("latest version is: " + packageRevision.getRevision());
        }
        return packageRevision;
    }

    /**
     * Returns the latest package revision of the artifact specified in the package configuration within the specified repository.
     * <p />
     * If a revision could not be found for the specified package criteria, this method will return {@code null}.
     *
     * @param packageConfig the package configuration (see {@link MavenPackageConfig})
     * @param repoConfig the repository configuration (see {@link MavenRepoConfig})
     * @param previouslyKnownRevision the last known package revision
     * @return the latest package revision or {@code null}
     * @throws RuntimeException if the specified configuration could not be validated successfully
     */
    public PackageRevisionMessage latestModificationSince(final PackageMaterialProperties packageConfig,
                                                          final PackageMaterialProperties repoConfig,
                                                          final PackageRevisionMessage previouslyKnownRevision) {
        LOGGER.info(String.format("check of latest for artifact with groupId: '%s', artifactId: '%s' in repo: %s, since version %s",
                        packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_GROUP_ID).orNull(),
                        packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID).orNull(),
                        repoConfig.getValue(ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_REPO_URL).orNull(),
                        previouslyKnownRevision.getRevision())
        );
        validateConfig(repoConfig, packageConfig);
        final PackageRevisionMessage updatedPackage = poll(new MavenRepoConfig(repoConfig), new MavenPackageConfig(packageConfig, previouslyKnownRevision));
        if (updatedPackage == null) {
            return null;
        } else {
            LOGGER.info("new latest version: " + updatedPackage.getRevision());
        }

        if (updatedPackage.getTimestamp().getTime() < previouslyKnownRevision.getTimestamp().getTime())
            LOGGER.warn(String.format("latest version %s published earlier (%s) than previous (%s, %s)",
                            updatedPackage.getRevision(),
                            updatedPackage.getTimestamp(),
                            previouslyKnownRevision.getRevision(),
                            previouslyKnownRevision.getTimestamp())
            );
        return updatedPackage;
    }

    /**
     * Checks the connection to the specified repository and returns the result.
     *
     * @param repoConfig the repository configuration (see {@link MavenRepoConfig})
     * @return the result of the connection check
     */
    public CheckConnectionResultMessage checkConnectionToRepository(final PackageMaterialProperties repoConfig) {
        try {
            final MavenRepoConfig mavenRepoConfig = new MavenRepoConfig(repoConfig);

            if (mavenRepoConfig.isRepoUrlMissing()) {
                return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, "Repo URL missing");
            }

            if (!new RepositoryConnector(mavenRepoConfig).testConnection()) {
                return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, "Did not get HTTP Status 200 response");
            }
        } catch (final Exception e) {
            return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, e.getMessage());
        }
        return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.SUCCESS);
    }

    /**
     * Checks the connection to the artifact specified in the package configuration within the specified repository and returns the result.
     *
     * @param packageConfig the package configuration (see {@link MavenPackageConfig})
     * @param repoConfig the repository configuration (see {@link MavenRepoConfig})
     * @return the result of the connection check
     */
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

    /**
     * Validates the configuration and throws a {@link RuntimeException} if the validation failed.
     *
     * @param packageConfig the package configuration (see {@link MavenPackageConfig})
     * @param repoConfig the repository configuration (see {@link MavenRepoConfig})
     * @throws RuntimeException if the specified configuration could not be validated successfully
     */
    private void validateConfig(final PackageMaterialProperties repoConfig, final PackageMaterialProperties packageConfig) {
        final ValidationResultMessage validationResult = new ConfigurationProvider().isRepositoryConfigurationValid(repoConfig);
        validationResult.addErrors(new ConfigurationProvider().isPackageConfigurationValid(packageConfig).getValidationErrors());

        if (!validationResult.success()) {
            final StringBuilder stringBuilder = new StringBuilder();
            for (final ValidationError validationError : validationResult.getValidationErrors()) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append("; ");
                }
                stringBuilder.append(validationError.getMessage());
            }
            
            throw new RuntimeException(stringBuilder.toString());
        }
    }

    /**
     * Polls the specified repository for the latest revision of the artifact as specified in the package configuration.
     *
     * @param packageConfig the package configuration (see {@link MavenPackageConfig})
     * @param repoConfig the repository configuration (see {@link MavenRepoConfig})
     * @return the latest revision or an empty package revision
     */
    private PackageRevisionMessage poll(final MavenRepoConfig repoConfig, final MavenPackageConfig packageConfig) {
        final MavenRevision latest = new RepositoryClient(repoConfig, packageConfig).getLatest();
        if (latest == null) {
            return null;
        }
        return latest.toPackageRevision();
    }
}

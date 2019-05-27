package com.oneandone.go.plugin.maven.config;

import com.oneandone.go.plugin.maven.message.PackageMaterialProperties;
import com.oneandone.go.plugin.maven.message.PackageRevisionMessage;
import com.oneandone.go.plugin.maven.message.ValidationError;
import com.oneandone.go.plugin.maven.message.ValidationResultMessage;
import com.oneandone.go.plugin.maven.util.MavenVersion;
import com.thoughtworks.go.plugin.api.logging.Logger;
import lombok.Getter;

import java.util.Optional;

/** Representation of a maven package configuration. */
public class MavenPackageConfig {

    /** The logging instance for this class. */
    private static final Logger LOGGER = Logger.getLoggerFor(MavenPackageConfig.class);

    /** The specified properties. */
    private final PackageMaterialProperties packageConfig;

    /**
     * The group id.
     *
     * @return the group id
     */
    @Getter private final String groupId;

    /**
     * The artifact id.
     *
     * @return the artifact id
     */
    @Getter private final String artifactId;

    /**
     * The lower version bound (inclusive).
     *
     * @return the lower version bound (inclusive)
     */
    @Getter private final MavenVersion lowerBound;

    /**
     * The upper version bound (exclusive).
     *
     * @return the upper version bound (exclusive)
     */
    @Getter private final MavenVersion upperBound;

    /**
     * The artifact packaging type.
     *
     * @return the artifact packaging type
     */
    @Getter private final String packaging;

    /**
     * The last known version.
     *
     * @return the last known version
     */
    @Getter private final String lastKnownVersion;

    /**
     * Constructs the packaging configuration by the specified properties.
     *
     * @param packageConfig the configuration properties
     * @param packageRevision the last known version
     */
    public MavenPackageConfig(final PackageMaterialProperties packageConfig, final PackageRevisionMessage packageRevision) {
        this.packageConfig = packageConfig;

        this.groupId = packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_GROUP_ID).orElse(null);
        this.artifactId = packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID).orElse(null);
        this.packaging = packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_PACKAGING).orElse(null);

        this.lowerBound = new MavenVersion(packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM).orElse(null);
        this.upperBound = new MavenVersion(packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_TO).orElse(null);

        this.lastKnownVersion = packageRevision != null ? packageRevision.getRevision() : null;
    }

    /**
     * Returns {@code true} if a lower version bound is specified, otherwise {@code false}.
     *
     * @return {@code true} if a lower version bound is specified, otherwise {@code false}
     * @see #lowerBound
     */
    public boolean lowerBoundGiven() {
        return lowerBound != null;
    }

    /**
     * Returns {@code true} if an upper version bound is specified, otherwise {@code false}.
     *
     * @return {@code true} if an upper version bound is specified, otherwise {@code false}
     * @see #upperBound
     */
    public boolean upperBoundGiven() {
        return upperBound != null;
    }

    /**
     * Returns {@code true} if the last version is not {@code null}, otherwise {@code false}.
     *
     * @return {@code true} if the last version is not {@code null}, otherwise {@code false}
     */
    public boolean isLastVersionKnown() {
        return lastKnownVersion != null;
    }

    /**
     * Validates an id and adds occurring errors to the validation result.
     *
     * @param errors the errors to append to
     * @param groupOrArtifactId the id to validate
     * @param what the configuration key that for the value that will be validated
     */
    private void validateId(final ValidationResultMessage errors, final String groupOrArtifactId, final String what) {
        if (groupOrArtifactId == null || groupOrArtifactId.trim().isEmpty()) {
            final String message = what + " is not specified";
            LOGGER.info(message);
            errors.addError(new ValidationError(what, message));
            return;
        }

        if ((groupOrArtifactId.contains("*") || groupOrArtifactId.contains("?"))) {
            final String message = String.format("%s [%s] is invalid", what, groupOrArtifactId);
            LOGGER.info(message);
            errors.addError(new ValidationError(what, message));
        }
    }

    /**
     * Validates the package configuration and returns the validation result.
     *
     * @return the validation result
     */
    public ValidationResultMessage validate() {
        final ValidationResultMessage validationResult = new ValidationResultMessage();
        validateId(validationResult, groupId, ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_GROUP_ID);
        validateId(validationResult, artifactId, ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID);

        boolean lowerBoundSpecified = false;
        final Optional<String> lowerBoundConfig = packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM);
        if (lowerBoundConfig.isPresent()) {
            lowerBoundSpecified = true;
            try {
                new MavenVersion(lowerBoundConfig.get());
            } catch (final IllegalArgumentException ex) {
                validationResult.addError(new ValidationError(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM, ex.getMessage()));
            }
        }

        boolean upperBoundSpecified = false;
        final Optional<String> upperBoundConfig = packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_TO);
        if (upperBoundConfig.isPresent()) {
            upperBoundSpecified = true;
            try {
                new MavenVersion(upperBoundConfig.get());
            } catch (final IllegalArgumentException ex) {
                validationResult.addError(new ValidationError(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_TO, ex.getMessage()));
            }
        }

        if (upperBoundSpecified && lowerBoundSpecified && new MavenVersion(lowerBoundConfig.get()).greaterOrEqual(new MavenVersion(upperBoundConfig.get()))) {
            validationResult.addError(new ValidationError(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM, "Lower Bound cannot be >= Upper Bound"));
        }

        ConfigurationProperties.detectInvalidKeys(packageConfig, validationResult,
                ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_GROUP_ID,
                ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID,
                ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_PACKAGING,
                ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM,
                ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_TO
        );
        return validationResult;
    }
}

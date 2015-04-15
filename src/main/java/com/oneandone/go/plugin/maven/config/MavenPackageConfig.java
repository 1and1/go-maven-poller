package com.oneandone.go.plugin.maven.config;

import com.google.common.base.Optional;
import com.oneandone.go.plugin.maven.message.PackageMaterialProperties;
import com.oneandone.go.plugin.maven.message.PackageRevisionMessage;
import com.oneandone.go.plugin.maven.message.ValidationError;
import com.oneandone.go.plugin.maven.message.ValidationResultMessage;
import com.oneandone.go.plugin.maven.util.MavenVersion;
import com.thoughtworks.go.plugin.api.logging.Logger;
import lombok.Getter;

public class MavenPackageConfig {

    public static final String INVALID_BOUNDS_MESSAGE = "Lower Bound cannot be >= Upper Bound";

    private static final Logger LOGGER = Logger.getLoggerFor(MavenPackageConfig.class);

    private final PackageMaterialProperties packageConfig;

    @Getter private final String groupId;
    @Getter private final String artifactId;
    @Getter private final MavenVersion lowerBound;
    @Getter private final MavenVersion upperBound;
    @Getter private final String packaging;
    @Getter private final String lastKnownVersion;

    public MavenPackageConfig(final PackageMaterialProperties packageConfig, final PackageRevisionMessage packageRevision) {
        this.packageConfig = packageConfig;

        this.groupId = packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_GROUP_ID).orNull();
        this.artifactId = packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID).orNull();
        this.packaging = packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_PACKAGING).orNull();

        if (packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM).isPresent()) {
            this.lowerBound = new MavenVersion(packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM).get());
        } else {
            this.lowerBound = null;
        }

        if (packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_TO).isPresent()) {
            this.upperBound = new MavenVersion(packageConfig.getValue(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_TO).get());
        } else {
            this.upperBound = null;
        }

        this.lastKnownVersion = packageRevision != null ? packageRevision.getRevision() : null;
    }

    public boolean lowerBoundGiven() {
        return lowerBound != null;
    }

    public boolean upperBoundGiven() {
        return upperBound != null;
    }

    public boolean isLastVersionKnown() {
        return lastKnownVersion != null;
    }

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
            validationResult.addError(new ValidationError(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM, INVALID_BOUNDS_MESSAGE));
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

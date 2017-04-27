package com.oneandone.go.plugin.maven.config;

import com.oneandone.go.plugin.maven.message.PackageMaterialProperties;
import com.oneandone.go.plugin.maven.message.PackageMaterialProperty;
import com.oneandone.go.plugin.maven.message.ValidationError;
import com.oneandone.go.plugin.maven.message.ValidationResultMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.TimeZone;

/** This class lists all available configuration properties. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurationProperties {

    /** The property key for the repository URL. */
    public static final String REPOSITORY_CONFIGURATION_KEY_REPO_URL = "REPO_URL";

    /** The property key for the authentication username. */
    public static final String REPOSITORY_CONFIGURATION_KEY_USERNAME = "USERNAME";

    /** The property key for the authentication password. */
    public static final String REPOSITORY_CONFIGURATION_KEY_PASSWORD = "PASSWORD";

    /** The property key for the HTTP proxy. */
    public static final String REPOSITORY_CONFIGURATION_KEY_PROXY = "PROXY";

    /** The property kez for the time zone of the repository. */
    public static final String REPOSITORY_CONFIGURATION_TIME_ZONE = "TIME_ZONE";

    /** The property key for the latest version tag to poll. */
    public static final String REPOSITORY_CONFIGURATION_KEY_LATEST_VERSION_TAG = "LATEST_VERSION_TAG";

    /** The property key for the group id. */
    public static final String PACKAGE_CONFIGURATION_KEY_GROUP_ID = "GROUP_ID";

    /** The property key for artifact id. */
    public static final String PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID = "ARTIFACT_ID";

    /** The property key for packaging type of the artifact. */
    public static final String PACKAGE_CONFIGURATION_KEY_PACKAGING = "PACKAGING";

    /** The property key for the version to poll from (inclusive). */
    public static final String PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM = "POLL_VERSION_FROM";

    /** The property key for the version to poll to (exclusive). */
    public static final String PACKAGE_CONFIGURATION_KEY_POLL_VERSION_TO = "POLL_VERSION_TO";

    /**
     * Returns the material property for {@link #REPOSITORY_CONFIGURATION_KEY_REPO_URL}.
     *
     * @return the material property for {@link #REPOSITORY_CONFIGURATION_KEY_REPO_URL}
     */
    public static PackageMaterialProperty getRepositoryConfigurationPropertyRepoUrl() {
        return new PackageMaterialProperty().withDisplayName("Maven Repo base URL").withDisplayOrder(0).withPartOfIdentity(true).withRequired(true);
    }

    /**
     * Returns the material property for {@link #REPOSITORY_CONFIGURATION_KEY_USERNAME}.
     *
     * @return the material property for {@link #REPOSITORY_CONFIGURATION_KEY_USERNAME}
     */
    public static PackageMaterialProperty getRepositoryConfigurationPropertyUsername() {
        return new PackageMaterialProperty().withDisplayName("UserName").withDisplayOrder(1);
    }

    /**
     * Returns the material property for {@link #REPOSITORY_CONFIGURATION_KEY_PASSWORD}.
     *
     * @return the material property for {@link #REPOSITORY_CONFIGURATION_KEY_PASSWORD}
     */
    public static PackageMaterialProperty getRepositoryConfigurationPropertyPassword() {
        return new PackageMaterialProperty().withDisplayName("Password").withDisplayOrder(2).withSecure(true);
    }

    /**
     * Returns the material property for {@link #REPOSITORY_CONFIGURATION_KEY_PROXY}.
     *
     * @return the material property for {@link #REPOSITORY_CONFIGURATION_KEY_PROXY}
     */
    public static PackageMaterialProperty getRepositoryConfigurationPropertyProxy() {
        return new PackageMaterialProperty().withDisplayName("Proxy").withDisplayOrder(3);
    }

    public static PackageMaterialProperty getRepositoryConfigurationTimeZone() {
        final String defaultTimeZone = TimeZone.getDefault().getID();
        return new PackageMaterialProperty().withDisplayName("Time zone").withValue(defaultTimeZone).withDefaultValue(defaultTimeZone).withDisplayOrder(4);
    }

    /**
     * Returns the material property for {@link #REPOSITORY_CONFIGURATION_KEY_LATEST_VERSION_TAG}.
     *
     * @return the material property for {@link #REPOSITORY_CONFIGURATION_KEY_LATEST_VERSION_TAG}
     */
    public static PackageMaterialProperty getRepositoryConfigurationPropertyLatestVersionTag() {
        return new PackageMaterialProperty().withDisplayName("Latest version Tag").withDisplayOrder(4);
    }

    /**
     * Returns the material property for {@link #PACKAGE_CONFIGURATION_KEY_GROUP_ID}.
     *
     * @return the material property for {@link #PACKAGE_CONFIGURATION_KEY_GROUP_ID}
     */
    public static PackageMaterialProperty getPackageConfigurationPropertyGroupId() {
        return new PackageMaterialProperty().withDisplayName("Group Id").withDisplayOrder(0).withPartOfIdentity(true).withRequired(true);
    }

    /**
     * Returns the material property for {@link #PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID}.
     *
     * @return the material property for {@link #PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID}
     */
    public static PackageMaterialProperty getPackageConfigurationPropertyArtifactId() {
        return new PackageMaterialProperty().withDisplayName("Artifact Id").withDisplayOrder(1).withPartOfIdentity(true).withRequired(true);
    }

    /**
     * Returns the material property for {@link #PACKAGE_CONFIGURATION_KEY_PACKAGING}.
     *
     * @return the material property for {@link #PACKAGE_CONFIGURATION_KEY_PACKAGING}
     */
    public static PackageMaterialProperty getPackageConfigurationPropertyPackaging() {
        return new PackageMaterialProperty().withDisplayName("Packaging (jar,war,ear...)").withDisplayOrder(2).withDefaultValue("jar").withPartOfIdentity(true).withRequired(true);
    }

    /**
     * Returns the material property for {@link #PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM}.
     *
     * @return the material property for {@link #PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM}
     */
    public static PackageMaterialProperty getPackageConfigurationPropertyPollVersionFrom() {
        return new PackageMaterialProperty().withDisplayName("Version to poll >=").withDisplayOrder(3).withPartOfIdentity(true);
    }

    /**
     * Returns the material property for {@link #PACKAGE_CONFIGURATION_KEY_POLL_VERSION_TO}.
     *
     * @return the material property for {@link #PACKAGE_CONFIGURATION_KEY_POLL_VERSION_TO}
     */
    public static PackageMaterialProperty getPackageConfigurationPropertyPollVersionTo() {
        return new PackageMaterialProperty().withDisplayName("Version to poll <").withDisplayOrder(4).withPartOfIdentity(true);
    }

    /**
     * Detects if all keys of the specified configuration are defined in {@code validKeys} and otherwise adds a validation error to {@code errors}.
     *
     * @param config the configuration to check
     * @param errors the errors to append to
     * @param validKeys the list of valid keys
     */
    public static void detectInvalidKeys(final PackageMaterialProperties config, final ValidationResultMessage errors, final String... validKeys) {
        for (final String key : config.keys()) {
            boolean valid = false;
            for (final String validKey : validKeys) {
                if (validKey.equals(key)) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                errors.addError(new ValidationError("", String.format("Unsupported key: %s. Valid keys: %s", key, Arrays.toString(validKeys))));
            }
        }
    }
}

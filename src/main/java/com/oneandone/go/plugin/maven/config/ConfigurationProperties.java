package com.oneandone.go.plugin.maven.config;

import com.oneandone.go.plugin.maven.message.PackageMaterialProperties;
import com.oneandone.go.plugin.maven.message.PackageMaterialProperty;
import com.oneandone.go.plugin.maven.message.ValidationError;
import com.oneandone.go.plugin.maven.message.ValidationResultMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurationProperties {

    public static final String REPOSITORY_CONFIGURATION_KEY_REPO_URL = "REPO_URL";
    public static final String REPOSITORY_CONFIGURATION_KEY_USERNAME = "USERNAME";
    public static final String REPOSITORY_CONFIGURATION_KEY_PASSWORD = "PASSWORD";
    public static final String REPOSITORY_CONFIGURATION_KEY_PROXY = "PROXY";

    public static final String PACKAGE_CONFIGURATION_KEY_GROUP_ID = "GROUP_ID";
    public static final String PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID = "ARTIFACT_ID";
    public static final String PACKAGE_CONFIGURATION_KEY_PACKAGING = "PACKAGING";
    public static final String PACKAGE_CONFIGURATION_KEY_POLL_VERSION_FROM = "POLL_VERSION_FROM";
    public static final String PACKAGE_CONFIGURATION_KEY_POLL_VERSION_TO = "POLL_VERSION_TO";

    public static final PackageMaterialProperty getRepositoryConfigurationPropertyRepoUrl() {
        return new PackageMaterialProperty().withDisplayName("Maven Repo base URL").withDisplayOrder(0).withPartOfIdentity(true).withRequired(true);
    }

    public static final PackageMaterialProperty getRepositoryConfigurationPropertyUsername() {
        return new PackageMaterialProperty().withDisplayName("UserName").withDisplayOrder(1);
    }

    public static final PackageMaterialProperty getRepositoryConfigurationPropertyPassword() {
        return new PackageMaterialProperty().withDisplayName("Password").withDisplayOrder(2).withSecure(true);
    }

    public static final PackageMaterialProperty getRepositoryConfigurationPropertyProxy() {
        return new PackageMaterialProperty().withDisplayName("Proxy").withDisplayOrder(3);
    }

    public static final PackageMaterialProperty getPackageConfigurationPropertyGroupId() {
        return new PackageMaterialProperty().withDisplayName("Group Id").withDisplayOrder(0).withPartOfIdentity(true).withRequired(true);
    }

    public static final PackageMaterialProperty getPackageConfigurationPropertyArtifactId() {
        return new PackageMaterialProperty().withDisplayName("Artifact Id").withDisplayOrder(1).withPartOfIdentity(true).withRequired(true);
    }

    public static final PackageMaterialProperty getPackageConfigurationPropertyPackaging() {
        return new PackageMaterialProperty().withDisplayName("Packaging (jar,war,ear...)").withDisplayOrder(2).withDefaultValue("jar").withPartOfIdentity(true).withRequired(true);
    }

    public static final PackageMaterialProperty getPackageConfigurationPropertyPollVersionFrom() {
        return new PackageMaterialProperty().withDisplayName("Version to poll >=").withDisplayOrder(3).withPartOfIdentity(true);
    }

    public static final PackageMaterialProperty getPackageConfigurationPropertyPollVersionTo() {
        return new PackageMaterialProperty().withDisplayName("Version to poll <").withDisplayOrder(4).withPartOfIdentity(true);
    }

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

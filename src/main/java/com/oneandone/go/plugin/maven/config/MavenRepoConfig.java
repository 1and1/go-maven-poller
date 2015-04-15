package com.oneandone.go.plugin.maven.config;

import com.oneandone.go.plugin.maven.message.PackageMaterialProperties;
import com.oneandone.go.plugin.maven.message.ValidationError;
import com.oneandone.go.plugin.maven.message.ValidationResultMessage;
import com.oneandone.go.plugin.maven.util.RepositoryURL;
import com.thoughtworks.go.plugin.api.logging.Logger;
import lombok.Getter;

import java.net.MalformedURLException;
import java.net.URL;

public class MavenRepoConfig {

    private static final Logger LOGGER = Logger.getLoggerFor(MavenRepoConfig.class);

    private PackageMaterialProperties repoConfig;
    private final String repositoryURL;

    @Getter private final String username;
    @Getter private final String password;
    @Getter private final String proxy;

    public MavenRepoConfig(final PackageMaterialProperties repoConfig) {
        this.repoConfig = repoConfig;

        this.repositoryURL = repoConfig.getValue(ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_REPO_URL).orNull();
        this.username = repoConfig.getValue(ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_USERNAME).orNull();
        this.password = repoConfig.getValue(ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_PASSWORD).orNull();
        this.proxy = repoConfig.getValue(ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_PROXY).orNull();
    }

    public RepositoryURL getRepoUrl() {
        final RepositoryURL repoUrl = new RepositoryURL(repositoryURL, username, password);
        if (!repoUrl.isHttp()) {
            throw new RuntimeException("Only http/https urls are supported");
        }
        return repoUrl;
    }

    public String getRepoUrlAsStringWithBasicAuth() {
        return this.getRepoUrl().getURLWithBasicAuth();
    }

    public String getRepoUrlAsString() {
        return this.getRepoUrl().getURL();
    }

    public boolean isRepoUrlMissing() {
        return repositoryURL == null || repositoryURL.trim().isEmpty();
    }

    public ValidationResultMessage validate() {
        final ValidationResultMessage validationResult = new ValidationResultMessage();
        if (isRepoUrlMissing()) {
            final String message = "Repository url not specified";
            LOGGER.error(message);
            validationResult.addError(new ValidationError(ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_REPO_URL, message));
            return validationResult;
        }

        try {
            final URL repoUrl = new URL(repositoryURL);

            if (!"http".equals(repoUrl.getProtocol().toLowerCase()) && !"https".equals(repoUrl.getProtocol().toLowerCase())) {
                validationResult.addError(new ValidationError(ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_REPO_URL, "Invalid URL: Only http is supported."));
            }

            if (repoUrl.getUserInfo() != null) {
                validationResult.addError(new ValidationError(ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_REPO_URL, "User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys."));
            }

        } catch (final MalformedURLException e) {
            LOGGER.error(e.getMessage());
            validationResult.addError(new ValidationError(ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_REPO_URL, "Malformed URL specified: " + e.getMessage()));
        }

        ConfigurationProperties.detectInvalidKeys(repoConfig, validationResult,
                ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_REPO_URL,
                ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_USERNAME,
                ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_PASSWORD,
                ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_PROXY
        );
        return validationResult;
    }
}

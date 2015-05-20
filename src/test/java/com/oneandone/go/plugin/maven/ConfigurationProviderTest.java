package com.oneandone.go.plugin.maven;

import com.oneandone.go.plugin.maven.config.ConfigurationProperties;
import com.oneandone.go.plugin.maven.config.ConfigurationProvider;
import com.oneandone.go.plugin.maven.message.PackageMaterialProperties;
import com.oneandone.go.plugin.maven.message.ValidationResultMessage;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Collection;

public class ConfigurationProviderTest {

    @Test
    public void testDetectInvalidKeys() throws Exception {
        final ConfigurationProvider configurationProvider = new ConfigurationProvider();
        final PackageMaterialProperties repositoryConfiguration = configurationProvider.getRepositoryConfiguration();

        ValidationResultMessage result = new ValidationResultMessage();
        ConfigurationProperties.detectInvalidKeys(repositoryConfiguration, result, "TEST", "FOO");
        assertFalse(result.success());

        result = new ValidationResultMessage();
        ConfigurationProperties.detectInvalidKeys(repositoryConfiguration, result,
                ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_REPO_URL,
                ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_USERNAME,
                ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_PASSWORD,
                ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_PROXY
        );
        assertTrue(result.success());
    }

    @Test
    public void testGetRepositoryConfiguration() throws Exception {
        final ConfigurationProvider configurationProvider = new ConfigurationProvider();
        final PackageMaterialProperties repositoryConfiguration = configurationProvider.getRepositoryConfiguration();
        assertNotNull(repositoryConfiguration);

        final Collection<String> properties = repositoryConfiguration.keys();
        assertFalse(properties.isEmpty());
        assertTrue(properties.contains("REPO_URL"));
    }

    @Test
    public void testGetPackageConfiguration() throws Exception {
        final ConfigurationProvider configurationProvider = new ConfigurationProvider();
        final PackageMaterialProperties packageConfiguration = configurationProvider.getPackageConfiguration();
        assertNotNull(packageConfiguration);
        assertFalse(packageConfiguration.keys().isEmpty());
    }

    @Test
    public void testIsRepositoryConfigurationValid() throws Exception {
        final ConfigurationProvider configurationProvider = new ConfigurationProvider();
        final PackageMaterialProperties repositoryConfiguration = configurationProvider.getRepositoryConfiguration();

        final ValidationResultMessage result = configurationProvider.isRepositoryConfigurationValid(repositoryConfiguration);
        assertFalse(result.success());
    }

    @Test
    public void testIsPackageConfigurationValid() throws Exception {
        final ConfigurationProvider configurationProvider = new ConfigurationProvider();
        final PackageMaterialProperties packageConfiguration = configurationProvider.getPackageConfiguration();

        final ValidationResultMessage result = configurationProvider.isPackageConfigurationValid(packageConfiguration);
        assertFalse(result.success());
    }
}
package com.oneandone.go.plugin.maven.config;

import com.oneandone.go.plugin.maven.message.ConfigurationMessage;
import com.oneandone.go.plugin.maven.util.JsonUtil;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;

import static org.junit.Assert.*;

public class MavenRepoConfigTest {

    private MavenRepoConfig repoConfig;

    @Before
    public void setUp() {
        final String configuration =
                "{" +
                "  \"repository-configuration\": {" +
                "    \"REPO_URL\": {" +
                "      \"value\": \"http://repo1.maven.org/maven2\"" +
                "    }," +
                "    \"USERNAME\": {" +
                "      \"value\": \"admin\"" +
                "    }," +
                "    \"PASSWORD\": {" +
                "      \"value\": \"12345\"" +
                "    },\n" +
                "    \"PROXY\": {\n" +
                "      \"value\": \"http://localhost:8080\"" +
                "    },\n" +
                "    \"TIME_ZONE\": {\n" +
                "      \"value\": \"UTC\"" +
                "    }" +
                "  }" +
                "}";
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        repoConfig = new MavenRepoConfig(configurationMessage.getRepositoryConfiguration());
    }

    @Test
    public void testGetRepoUrlAsStringWithBasicAuth() {
        assertEquals("http://admin:12345@repo1.maven.org/maven2/", repoConfig.getRepoUrlAsStringWithBasicAuth());
    }

    @Test
    public void testGetRepoUrlAsString() {
        assertEquals("http://repo1.maven.org/maven2/", repoConfig.getRepoUrlAsString());
    }

    @Test
    public void testIsRepoUrlNotMissing() {
        assertFalse(repoConfig.isRepoUrlMissing());
    }

    @Test
    public void testWithRepoUrlMissing() {
        final String configuration =
                "{" +
                        "  \"repository-configuration\": {" +
                        "    \"USERNAME\": {" +
                        "      \"value\": \"admin\"" +
                        "    }," +
                        "    \"PASSWORD\": {" +
                        "      \"value\": \"12345\"" +
                        "    },\n" +
                        "    \"PROXY\": {\n" +
                        "      \"value\": \"http://localhost:8080\"" +
                        "    }" +
                        "  }" +
                        "}";
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        repoConfig = new MavenRepoConfig(configurationMessage.getRepositoryConfiguration());

        assertTrue(repoConfig.isRepoUrlMissing());
        assertFalse(repoConfig.validate().success());
    }


    @Test
    public void testValidate() {
        assertTrue(repoConfig.validate().success());
    }

    @Test
    public void testGetUsername() {
        assertEquals("admin", repoConfig.getUsername());
    }

    @Test
    public void testGetPassword() {
        assertEquals("12345", repoConfig.getPassword());
    }

    @Test
    public void testGetProxy() {
        assertEquals("http://localhost:8080", repoConfig.getProxy());
    }

    @Test
    public void testGetTimeZone() {
        assertEquals(ZoneId.of("UTC"), repoConfig.getTimeZone());
    }

    @Test
    public void testRepoConfigWithFoobarTimeZone() {
        final String configuration =
                "{" +
                        "  \"repository-configuration\": {" +
                        "    \"REPO_URL\": {" +
                        "      \"value\": \"http://repo1.maven.org/maven2\"" +
                        "    }," +
                        "    \"TIME_ZONE\": {\n" +
                        "      \"value\": \"Foobar123Whatever\"" +
                        "    }" +
                        "  }" +
                        "}";
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        repoConfig = new MavenRepoConfig(configurationMessage.getRepositoryConfiguration());

        assertEquals(ZoneId.of("GMT"), repoConfig.getTimeZone());
    }
}
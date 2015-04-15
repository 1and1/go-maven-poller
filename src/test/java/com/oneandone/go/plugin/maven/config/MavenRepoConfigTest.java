package com.oneandone.go.plugin.maven.config;

import com.oneandone.go.plugin.maven.message.ConfigurationMessage;
import com.oneandone.go.plugin.maven.util.JsonUtil;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MavenRepoConfigTest {

    private MavenRepoConfig repoConfig;

    @Before
    public void setUp() throws Exception {
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
                "    }" +
                "  }" +
                "}";
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        repoConfig = new MavenRepoConfig(configurationMessage.getRepositoryConfiguration());
    }

    @Test
    public void testGetRepoUrlAsStringWithBasicAuth() throws Exception {
        assertEquals("http://admin:12345@repo1.maven.org/maven2/", repoConfig.getRepoUrlAsStringWithBasicAuth());
    }

    @Test
    public void testGetRepoUrlAsString() throws Exception {
        assertEquals("http://repo1.maven.org/maven2/", repoConfig.getRepoUrlAsString());
    }

    @Test
    public void testIsRepoUrlNotMissing() throws Exception {
        assertFalse(repoConfig.isRepoUrlMissing());
    }

    @Test
    public void testWithRepoUrlMissing() throws Exception {
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
    public void testValidate() throws Exception {
        assertTrue(repoConfig.validate().success());
    }

    @Test
    public void testGetUsername() throws Exception {
        assertEquals("admin", repoConfig.getUsername());
    }

    @Test
    public void testGetPassword() throws Exception {
        assertEquals("12345", repoConfig.getPassword());
    }

    @Test
    public void testGetProxy() throws Exception {
        assertEquals("http://localhost:8080", repoConfig.getProxy());
    }
}
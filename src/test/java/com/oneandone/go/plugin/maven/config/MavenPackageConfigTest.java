package com.oneandone.go.plugin.maven.config;

import com.oneandone.go.plugin.maven.message.ConfigurationMessage;
import com.oneandone.go.plugin.maven.util.JsonUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class MavenPackageConfigTest {

    private MavenPackageConfig packageConfig;

    @Before
    public void setUp() {
        final String configuration =
                "{" +
                        "  \"package-configuration\": {" +
                        "    \"GROUP_ID\": {" +
                        "      \"value\": \"com.oneandone\"" +
                        "    }," +
                        "    \"ARTIFACT_ID\": {" +
                        "      \"value\": \"go-maven-poller\"" +
                        "    },\n" +
                        "    \"PACKAGING\": {" +
                        "      \"value\": \"jar\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_FROM\": {" +
                        "      \"value\": \"0.1.0\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_TO\": {" +
                        "      \"value\": \"2.0.0\"" +
                        "    }" +
                        "  }" +
                        "}";
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        packageConfig = new MavenPackageConfig(configurationMessage.getPackageConfiguration(), null);
    }

    @Test
    public void testAccessor() {
        assertEquals("com.oneandone", packageConfig.getGroupId());
        assertEquals("go-maven-poller", packageConfig.getArtifactId());
        assertEquals("jar", packageConfig.getPackaging());

        assertEquals(0, packageConfig.getLowerBound().getMajor());
        assertEquals(2, packageConfig.getUpperBound().getMajor());
    }

    @Test
    public void testLowerBoundGiven() {
        assertTrue(packageConfig.lowerBoundGiven());
    }

    @Test
    public void testUpperBoundGiven() {
        assertTrue(packageConfig.upperBoundGiven());
    }

    @Test
    public void testIsLastVersionKnown() {
        assertFalse(packageConfig.isLastVersionKnown());
    }

    @Test
    public void testValidate() {
        assertTrue(packageConfig.validate().success());
    }

    @Test
    public void testValidationWithMissingGroupId() {
        final String configuration =
                "{" +
                        "  \"package-configuration\": {" +
                        "    \"ARTIFACT_ID\": {" +
                        "      \"value\": \"go-maven-poller\"" +
                        "    },\n" +
                        "    \"PACKAGING\": {" +
                        "      \"value\": \"jar\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_FROM\": {" +
                        "      \"value\": \"0.1.0\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_TO\": {" +
                        "      \"value\": \"2.0.0\"" +
                        "    }" +
                        "  }" +
                        "}";
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        packageConfig = new MavenPackageConfig(configurationMessage.getPackageConfiguration(), null);
        assertFalse(packageConfig.validate().success());
    }

    @Test
    public void testValidationWithIllegalGroupId() {
        final String configuration =
                "{" +
                        "  \"package-configuration\": {" +
                        "    \"GROUP_ID\": {" +
                        "      \"value\": \"*\"" +
                        "    }," +
                        "    \"ARTIFACT_ID\": {" +
                        "      \"value\": \"go-maven-poller\"" +
                        "    },\n" +
                        "    \"PACKAGING\": {" +
                        "      \"value\": \"jar\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_FROM\": {" +
                        "      \"value\": \"0.1.0\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_TO\": {" +
                        "      \"value\": \"2.0.0\"" +
                        "    }" +
                        "  }" +
                        "}";
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        packageConfig = new MavenPackageConfig(configurationMessage.getPackageConfiguration(), null);
        assertFalse(packageConfig.validate().success());
    }

    @Test
    public void testValidationWithMissingArtifactId() {
        final String configuration =
                "{" +
                        "  \"package-configuration\": {" +
                        "    \"GROUP_ID\": {" +
                        "      \"value\": \"com.oneandone\"" +
                        "    }," +
                        "    \"PACKAGING\": {" +
                        "      \"value\": \"jar\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_FROM\": {" +
                        "      \"value\": \"0.1.0\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_TO\": {" +
                        "      \"value\": \"2.0.0\"" +
                        "    }" +
                        "  }" +
                        "}";
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        packageConfig = new MavenPackageConfig(configurationMessage.getPackageConfiguration(), null);
        assertFalse(packageConfig.validate().success());
    }

    @Test
    public void testValidationWithIllegalArtifactId() {
        final String configuration =
                "{" +
                        "  \"package-configuration\": {" +
                        "    \"GROUP_ID\": {" +
                        "      \"value\": \"com.oneandone\"" +
                        "    }," +
                        "    \"ARTIFACT_ID\": {" +
                        "      \"value\": \"*\"" +
                        "    },\n" +
                        "    \"PACKAGING\": {" +
                        "      \"value\": \"jar\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_FROM\": {" +
                        "      \"value\": \"0.1.0\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_TO\": {" +
                        "      \"value\": \"2.0.0\"" +
                        "    }" +
                        "  }" +
                        "}";
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        packageConfig = new MavenPackageConfig(configurationMessage.getPackageConfiguration(), null);
        assertFalse(packageConfig.validate().success());
    }

    @Ignore
    @Test
    public void testValidationWithIllegalLowerBound() {
        final String configuration =
                "{" +
                        "  \"package-configuration\": {" +
                        "    \"GROUP_ID\": {" +
                        "      \"value\": \"com.oneandone\"" +
                        "    }," +
                        "    \"ARTIFACT_ID\": {" +
                        "      \"value\": \"go-maven-poller\"" +
                        "    },\n" +
                        "    \"PACKAGING\": {" +
                        "      \"value\": \"jar\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_FROM\": {" +
                        "      \"value\": \"foobar*?.a.2.wtf\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_TO\": {" +
                        "      \"value\": \"2.0.0\"" +
                        "    }" +
                        "  }" +
                        "}";
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        packageConfig = new MavenPackageConfig(configurationMessage.getPackageConfiguration(), null);
        assertFalse(packageConfig.validate().success());
    }

    @Test
    public void testValidationWithIllegalUpperBound() {
        final String configuration =
                "{" +
                        "  \"package-configuration\": {" +
                        "    \"GROUP_ID\": {" +
                        "      \"value\": \"com.oneandone\"" +
                        "    }," +
                        "    \"ARTIFACT_ID\": {" +
                        "      \"value\": \"go-maven-poller\"" +
                        "    },\n" +
                        "    \"PACKAGING\": {" +
                        "      \"value\": \"jar\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_FROM\": {" +
                        "      \"value\": \"0.1.0\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_TO\": {" +
                        "      \"value\": \"foobar*?.a.2.wtf\"" +
                        "    }" +
                        "  }" +
                        "}";
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        packageConfig = new MavenPackageConfig(configurationMessage.getPackageConfiguration(), null);
        assertFalse(packageConfig.validate().success());
    }

    @Test
    public void testValidationWithLowerBoundGreaterThanUpperBound() {
        final String configuration =
                "{" +
                        "  \"package-configuration\": {" +
                        "    \"GROUP_ID\": {" +
                        "      \"value\": \"com.oneandone\"" +
                        "    }," +
                        "    \"ARTIFACT_ID\": {" +
                        "      \"value\": \"go-maven-poller\"" +
                        "    },\n" +
                        "    \"PACKAGING\": {" +
                        "      \"value\": \"jar\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_FROM\": {" +
                        "      \"value\": \"1337.0.0\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_TO\": {" +
                        "      \"value\": \"2.0.0\"" +
                        "    }" +
                        "  }" +
                        "}";
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        packageConfig = new MavenPackageConfig(configurationMessage.getPackageConfiguration(), null);
        assertFalse(packageConfig.validate().success());
    }
}
package com.oneandone.go.plugin.maven;

import com.oneandone.go.plugin.maven.config.ConfigurationProperties;
import com.oneandone.go.plugin.maven.message.*;
import com.oneandone.go.plugin.maven.util.JsonUtil;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MavenRepositoryMaterialTest {

    private static EmbeddedHttpServer embeddedHttpServer;

    @BeforeClass
    public static void setUpLocalWebServer() {
        embeddedHttpServer = new EmbeddedHttpServer().withPath(new File("src/test/resources/web"));
        embeddedHttpServer.start();
    }

    @AfterClass
    public static void stopLocalWebServer() {
        embeddedHttpServer.stop();
    }

    @Test
    public void testHandle() throws Exception {
        int runningPort = embeddedHttpServer.getRunningPort();

        final MavenRepositoryMaterial repositoryMaterial = new MavenRepositoryMaterial();

        // request get conf
        GoPluginApiResponse response = repositoryMaterial.handle(request("go.plugin-settings.get-configuration", null));
        assertNotNull(response);
        assertEquals(200, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        assertEquals("{}", response.responseBody());

        // request repo conf
        response = repositoryMaterial.handle(request("repository-configuration", null));
        PackageMaterialProperties properties = JsonUtil.fromJsonString(response.responseBody(), PackageMaterialProperties.class);
        assertNotNull(properties);

        // request package conf
        response = repositoryMaterial.handle(request("package-configuration", null));
        properties = JsonUtil.fromJsonString(response.responseBody(), PackageMaterialProperties.class);
        assertNotNull(properties);

        // validate configurations
        final String configuration = JsonUtil.toJsonString(configuration());
        response = repositoryMaterial.handle(request("validate-repository-configuration", configuration));
        assertEquals(200, response.responseCode());

        response = repositoryMaterial.handle(request("validate-package-configuration", configuration));
        assertEquals(200, response.responseCode());

        // check repository connection
        response = repositoryMaterial.handle(request("check-repository-connection", configuration));
        assertNotNull(response);

        CheckConnectionResultMessage connectionResultMessage = JsonUtil.fromJsonString(response.responseBody(), CheckConnectionResultMessage.class);
        assertTrue(connectionResultMessage.success());

        // get latest package revision
        response = repositoryMaterial.handle(request("latest-revision", configuration));
        final PackageRevisionMessage revisionMessage = JsonUtil.fromJsonString(response.responseBody(), PackageRevisionMessage.class);
        assertNotNull(revisionMessage);
        assertEquals("2.1.0-SNAPSHOT (20150409.112032-10)", revisionMessage.getRevision());
        final LocalDateTime localdate = LocalDateTime.parse("20150409112033", DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        assertEquals(ZonedDateTime.of(localdate, ZoneId.systemDefault()), revisionMessage.getTimestamp().withZoneSameInstant(ZoneId.systemDefault()));
        assertEquals(2015, revisionMessage.getTimestamp().getYear());
        assertEquals(Month.APRIL, revisionMessage.getTimestamp().getMonth());
        assertEquals(9, revisionMessage.getTimestamp().getDayOfMonth());
        assertEquals(11, revisionMessage.getTimestamp().getHour());
        assertEquals(20, revisionMessage.getTimestamp().getMinute());
        assertEquals(33, revisionMessage.getTimestamp().getSecond());
        assertEquals("http://localhost:" + runningPort + "/com/oneandone/network/rrd-client-ra/2.1.0-SNAPSHOT/rrd-client-ra-2.1.0-20150409.112032-10.rar", revisionMessage.getDataFor("LOCATION"));

        // check package connection
        response = repositoryMaterial.handle(request("check-repository-connection", configuration));
        connectionResultMessage = JsonUtil.fromJsonString(response.responseBody(), CheckConnectionResultMessage.class);
        assertTrue(connectionResultMessage.success());

        // get latest revision since
        response = repositoryMaterial.handle(request("latest-revision-since", JsonUtil.toJsonString(configuration(revisionMessage))));
        assertNull(response.responseBody());
    }

    @Test
    public void testPluginIdentifier() {
        final MavenRepositoryMaterial material = new MavenRepositoryMaterial();
        assertEquals("package-repository", material.pluginIdentifier().getExtension());
        assertTrue(material.pluginIdentifier().getSupportedExtensionVersions().contains("1.0"));
    }

    @Test
    public void testSanitizeRequestBodyWithNull() {
        String result = MavenRepositoryMaterial.sanitizeRequestBody(null);
        assertNull(result);
    }

    @Test
    public void testSanitizeRequestBodyWithEmptyString() {
        String result = MavenRepositoryMaterial.sanitizeRequestBody("");
        assertEquals("", result);
    }

    @Test
    public void testSanitizeRequestBodyWithUnparsable() {
        String result = MavenRepositoryMaterial.sanitizeRequestBody("[]");
        assertEquals("[]", result);
    }

    @Test
    public void testSanitizeRequestBodyWithNoChange() {
        ConfigurationMessage configurationMessage = new ConfigurationMessage();
        String json = JsonUtil.toJsonString(configurationMessage);
        String result = MavenRepositoryMaterial.sanitizeRequestBody(json);
        assertEquals(json, result);
    }

    @Test
    public void testSanitizeRequestBodyWithPasswordReplaced() throws NoSuchFieldException, IllegalAccessException {
        final ConfigurationMessage configurationMessage = new ConfigurationMessage();
        final Class<ConfigurationMessage> messageClass = ConfigurationMessage.class;
        final Map<String, PackageMaterialProperty> repositoryConfiguration = new HashMap<>();

        final Field repositoryConfigurationField = messageClass.getDeclaredField("repositoryConfiguration");
        repositoryConfigurationField.setAccessible(true);
        repositoryConfigurationField.set(configurationMessage, repositoryConfiguration);
        repositoryConfiguration.put(
                ConfigurationProperties.REPOSITORY_CONFIGURATION_KEY_PASSWORD,
                new PackageMaterialProperty().withSecure(true).withValue("replace_me"));

        String json = JsonUtil.toJsonString(configurationMessage);
        String result = MavenRepositoryMaterial.sanitizeRequestBody(json);

        // password should be replaced
        assertNotEquals(json, result);

        // this is how it should look like
        assertEquals(json.replaceAll("replace_me", "********"), result);
    }

    private static GoPluginApiRequest request(final String requestName, final String requestBody) {
        return new GoPluginApiRequest() {
            @Override
            public String extension() {
                return null;
            }

            @Override
            public String extensionVersion() {
                return null;
            }

            @Override
            public String requestName() {
                return requestName;
            }

            @Override
            public Map<String, String> requestParameters() {
                return null;
            }

            @Override
            public Map<String, String> requestHeaders() {
                return null;
            }

            @Override
            public String requestBody() {
                return requestBody;
            }
        };
    }

    private static ConfigurationMessage configuration() throws NoSuchFieldException, IllegalAccessException {
        return configuration(null);
    }

    private static ConfigurationMessage configuration(final PackageRevisionMessage revisionMessage) throws NoSuchFieldException, IllegalAccessException {
        final ConfigurationMessage configurationMessage = new ConfigurationMessage();
        final Class<ConfigurationMessage> messageClass = ConfigurationMessage.class;

        final Field repositoryConfigurationField = messageClass.getDeclaredField("repositoryConfiguration");
        repositoryConfigurationField.setAccessible(true);
        final Map<String, PackageMaterialProperty> repositoryConfiguration = new HashMap<>();
        repositoryConfiguration.put("REPO_URL", new PackageMaterialProperty().withValue("http://localhost:" + embeddedHttpServer.getRunningPort()));
        repositoryConfigurationField.set(configurationMessage, repositoryConfiguration);

        final Field packageConfigurationField = messageClass.getDeclaredField("packageConfiguration");
        packageConfigurationField.setAccessible(true);
        final Map<String, PackageMaterialProperty> packageConfiguration = new HashMap<>();
        packageConfiguration.put(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_GROUP_ID, new PackageMaterialProperty().withValue("com.oneandone.network"));
        packageConfiguration.put(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_ARTIFACT_ID, new PackageMaterialProperty().withValue("rrd-client-ra"));
        packageConfiguration.put(ConfigurationProperties.PACKAGE_CONFIGURATION_KEY_PACKAGING, new PackageMaterialProperty().withValue("rar"));
        packageConfigurationField.set(configurationMessage, packageConfiguration);

        final Field previousRevisonField = messageClass.getDeclaredField("previousRevision");
        previousRevisonField.setAccessible(true);
        previousRevisonField.set(configurationMessage, revisionMessage);

        return configurationMessage;
    }
}
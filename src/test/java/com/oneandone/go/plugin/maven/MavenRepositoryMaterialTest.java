package com.oneandone.go.plugin.maven;

import com.oneandone.go.plugin.maven.message.*;
import com.oneandone.go.plugin.maven.util.JsonUtil;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class MavenRepositoryMaterialTest {

    private static Server server;
    private static Integer runningPort;

    @BeforeClass
    public static void setUpLocalWebServer() throws Exception {
        server = new Server();

        final SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(0);
        server.addConnector(connector);

        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setResourceBase(new File("src/test/resources/web").getAbsolutePath());

        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resourceHandler, new DefaultHandler()});
        server.setHandler(handlers);

        final Thread serverThread = new Thread() {
            @Override
            public void run() {
                try {
                    server.start();
                    runningPort = server.getConnectors()[0].getLocalPort();
                    server.join();
                } catch (final Exception e) {
                    e.printStackTrace();
                }

            }
        };
        Executors.newSingleThreadExecutor().submit(serverThread);
    }

    @Test
    public void testHandle() throws Exception {
        while (runningPort == null) {
            Thread.sleep(100);
        }

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
        assertEquals("2.1.0-SNAPSHOT", revisionMessage.getRevision());
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
    public void testPluginIdentifier() throws Exception {
        final MavenRepositoryMaterial material = new MavenRepositoryMaterial();
        assertEquals("package-repository", material.pluginIdentifier().getExtension());
        assertTrue(material.pluginIdentifier().getSupportedExtensionVersions().contains("1.0"));
    }

    @AfterClass
    public static void stopLocalWebServer() throws Exception {
        server.stop();
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
        repositoryConfiguration.put("REPO_URL", new PackageMaterialProperty().withValue("http://localhost:" + runningPort));
        repositoryConfigurationField.set(configurationMessage, repositoryConfiguration);

        final Field packageConfigurationField = messageClass.getDeclaredField("packageConfiguration");
        packageConfigurationField.setAccessible(true);
        final Map<String, PackageMaterialProperty> packageConfiguration = new HashMap<>();
        packageConfiguration.put("GROUP_ID", new PackageMaterialProperty().withValue("com.oneandone.network"));
        packageConfiguration.put("ARTIFACT_ID", new PackageMaterialProperty().withValue("rrd-client-ra"));
        packageConfiguration.put("PACKAGING", new PackageMaterialProperty().withValue("rar"));
        packageConfigurationField.set(configurationMessage, packageConfiguration);

        final Field previousRevisonField = messageClass.getDeclaredField("previousRevision");
        previousRevisonField.setAccessible(true);
        previousRevisonField.set(configurationMessage, revisionMessage);

        return configurationMessage;
    }
}
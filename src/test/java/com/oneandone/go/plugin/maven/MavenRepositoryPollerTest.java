package com.oneandone.go.plugin.maven;

import com.oneandone.go.plugin.maven.message.CheckConnectionResultMessage;
import com.oneandone.go.plugin.maven.message.ConfigurationMessage;
import com.oneandone.go.plugin.maven.message.PackageMaterialProperties;
import com.oneandone.go.plugin.maven.message.PackageRevisionMessage;
import com.oneandone.go.plugin.maven.util.JsonUtil;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MavenRepositoryPollerTest {

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

    private PackageMaterialProperties repositoryConfiguration;
    private PackageMaterialProperties packageConfiguration;

    @Before
    public void setUp() throws Exception {
        while (runningPort == null) {
            Thread.sleep(100);
        }

        final String configuration =
                "{" +
                        "  \"repository-configuration\": {" +
                        "    \"REPO_URL\": {" +
                        "      \"value\": \"http://localhost:" + runningPort + "/\"" +
                        "    }" +
                        "  }," +
                        "  \"package-configuration\": {" +
                        "    \"GROUP_ID\": {" +
                        "      \"value\": \"mysql\"" +
                        "    }," +
                        "    \"ARTIFACT_ID\": {" +
                        "      \"value\": \"mysql-connector-java\"" +
                        "    },\n" +
                        "    \"PACKAGING\": {" +
                        "      \"value\": \"jar\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_FROM\": {" +
                        "      \"value\": \"5.1.0\"" +
                        "    },\n" +
                        "    \"POLL_VERSION_TO\": {" +
                        "      \"value\": \"5.1.15\"" +
                        "    }" +
                        "  }" +
                        "}";
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);

        repositoryConfiguration = configurationMessage.getRepositoryConfiguration();
        packageConfiguration = configurationMessage.getPackageConfiguration();
    }

    @Test
    public void testGetLatestRevision() throws Exception {
        final MavenRepositoryPoller mavenPoller = new MavenRepositoryPoller();
        final PackageRevisionMessage latestRevision = mavenPoller.getLatestRevision(packageConfiguration, repositoryConfiguration);
        assertEquals("5.1.14", latestRevision.getRevision());
    }

    @Test
    public void testLatestModificationSince() throws Exception {
        final MavenRepositoryPoller mavenPoller = new MavenRepositoryPoller();
        PackageRevisionMessage revisionMessage = new PackageRevisionMessage("5.1.0",
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(0L), ZoneId.systemDefault() ), null, null, null);
        final PackageRevisionMessage packageRevision = mavenPoller.latestModificationSince(packageConfiguration, repositoryConfiguration, revisionMessage);
        assertEquals("5.1.14", packageRevision.getRevision());
    }

    @Test
    public void testCheckConnectionToRepository() throws Exception {
        final MavenRepositoryPoller mavenPoller = new MavenRepositoryPoller();
        final CheckConnectionResultMessage result = mavenPoller.checkConnectionToRepository(repositoryConfiguration);
        assertTrue(result.success());
    }

    @Test
    public void testCheckConnectionToPackage() throws Exception {
        final MavenRepositoryPoller mavenPoller = new MavenRepositoryPoller();
        final CheckConnectionResultMessage result = mavenPoller.checkConnectionToPackage(packageConfiguration, repositoryConfiguration);
        assertTrue(result.success());
    }

    @AfterClass
    public static void stopLocalWebServer() throws Exception {
        server.stop();
    }
}
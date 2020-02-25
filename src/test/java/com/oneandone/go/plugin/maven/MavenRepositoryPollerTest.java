package com.oneandone.go.plugin.maven;

import com.oneandone.go.plugin.maven.message.CheckConnectionResultMessage;
import com.oneandone.go.plugin.maven.message.ConfigurationMessage;
import com.oneandone.go.plugin.maven.message.PackageMaterialProperties;
import com.oneandone.go.plugin.maven.message.PackageRevisionMessage;
import com.oneandone.go.plugin.maven.util.JsonUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MavenRepositoryPollerTest {

    private static EmbeddedHttpServer embeddedHttpServer;

    @BeforeClass
    public static void setUpLocalWebServer() {
        embeddedHttpServer = new EmbeddedHttpServer(new File("src/test/resources/web"));
        embeddedHttpServer.start();
    }

    @AfterClass
    public static void stopLocalWebServer() throws Exception {
        embeddedHttpServer.stop();
    }

    private PackageMaterialProperties repositoryConfiguration;
    private PackageMaterialProperties packageConfiguration;

    @Before
    public void setUp() throws Exception {
        int runningPort = embeddedHttpServer.getRunningPort();

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
    public void testGetLatestRevision() {
        final MavenRepositoryPoller mavenPoller = new MavenRepositoryPoller();
        final PackageRevisionMessage latestRevision = mavenPoller.getLatestRevision(packageConfiguration, repositoryConfiguration);
        assertEquals("5.1.14", latestRevision.getRevision());
    }

    @Test
    public void testLatestModificationSince() {
        final MavenRepositoryPoller mavenPoller = new MavenRepositoryPoller();
        PackageRevisionMessage revisionMessage = new PackageRevisionMessage("5.1.0",
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(0L), ZoneId.systemDefault() ), null, null, null);
        final PackageRevisionMessage packageRevision = mavenPoller.latestModificationSince(packageConfiguration, repositoryConfiguration, revisionMessage);
        assertEquals("5.1.14", packageRevision.getRevision());
    }

    @Test
    public void testCheckConnectionToRepository() {
        final MavenRepositoryPoller mavenPoller = new MavenRepositoryPoller();
        final CheckConnectionResultMessage result = mavenPoller.checkConnectionToRepository(repositoryConfiguration);
        assertTrue(result.success());
    }

    @Test
    public void testCheckConnectionToPackage() {
        final MavenRepositoryPoller mavenPoller = new MavenRepositoryPoller();
        final CheckConnectionResultMessage result = mavenPoller.checkConnectionToPackage(packageConfiguration, repositoryConfiguration);
        assertTrue(result.success());
    }
}
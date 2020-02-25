package com.oneandone.go.plugin.maven.client;

import com.oneandone.go.plugin.maven.config.MavenPackageConfig;
import com.oneandone.go.plugin.maven.config.MavenRepoConfig;
import com.oneandone.go.plugin.maven.message.ConfigurationMessage;
import com.oneandone.go.plugin.maven.util.JsonUtil;
import com.oneandone.go.plugin.maven.util.MavenArtifactFiles;
import com.oneandone.go.plugin.maven.util.MavenRevision;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RepositoryClient.class, MavenArtifactFiles.class})
@PowerMockIgnore({"jdk.*", "javax.xml.*", "com.sun.*", "org.w3c.*", "org.xml.*"})
public class RepositoryClientTest {

    private String metadata;
    private String metadataWithReleaseTag;

    @Before
    public void setUp() throws Exception {
        final InputStream stream = RepositoryResponseHandlerTest.class.getClassLoader().getResourceAsStream("web/mysql/mysql-connector-java/maven-metadata.xml");
        final StringWriter writer = new StringWriter();

        IOUtils.copy(stream, writer, StandardCharsets.UTF_8);
        metadata = writer.toString();

        final InputStream streamWithReleaseTag = RepositoryResponseHandlerTest.class.getClassLoader().getResourceAsStream("web/mysql/mysql-connector-java/maven-metadata-with-release-tag.xml");
        final StringWriter writerWithReleaseTag = new StringWriter();

        IOUtils.copy(streamWithReleaseTag, writerWithReleaseTag, StandardCharsets.UTF_8);
        metadataWithReleaseTag = writerWithReleaseTag.toString();
    }

    @Test
    public void testGetLatestWithoutTag() throws Exception {
        final String configuration =
                "{" +
                        "  \"repository-configuration\": {" +
                        "    \"REPO_URL\": {" +
                        "      \"value\": \"http://repo1.maven.org/maven2\"" +
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

        final RepositoryClient client = getRepositoryClient(configuration, metadata);
        final MavenRevision revision = client.getLatest();

        assertEquals(5, revision.getMajor());
        assertEquals(1, revision.getMinor());
        assertEquals(14, revision.getBugfix());
    }

    @Test
    public void testGetLatestWithReleaseTag() throws Exception {
        final String configuration =
                "{" +
                        "  \"repository-configuration\": {" +
                        "    \"REPO_URL\": {" +
                        "      \"value\": \"http://repo1.maven.org/maven2\"" +
                        "    }," +
                        "    \"LATEST_VERSION_TAG\": {" +
                        "      \"value\": \"release\"" +
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
                        "    }\n" +
                        "  }" +
                        "}";

        final RepositoryClient client = getRepositoryClient(configuration, metadataWithReleaseTag);
        final MavenRevision revision = client.getLatest();

        assertEquals(5, revision.getMajor());
        assertEquals(1, revision.getMinor());
        assertEquals(18, revision.getBugfix());
    }

    @Test
    public void testGetLatestWithLatestTag() throws Exception {
        final String configuration =
                "{" +
                        "  \"repository-configuration\": {" +
                        "    \"REPO_URL\": {" +
                        "      \"value\": \"http://repo1.maven.org/maven2\"" +
                        "    }," +
                        "    \"LATEST_VERSION_TAG\": {" +
                        "      \"value\": \"latest\"" +
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
                        "    }\n" +
                        "  }" +
                        "}";

        final RepositoryClient client = getRepositoryClient(configuration, metadata);
        final MavenRevision revision = client.getLatest();

        assertEquals(2, revision.getMajor());
        assertEquals(0, revision.getMinor());
        assertEquals(14, revision.getBugfix());
    }

    @Test
    public void testGetLatestWithSpecifiedTagButNoTagInMavenMetadata() throws Exception {
        final String configuration =
                "{" +
                        "  \"repository-configuration\": {" +
                        "    \"REPO_URL\": {" +
                        "      \"value\": \"http://repo1.maven.org/maven2\"" +
                        "    }," +
                        "    \"LATEST_VERSION_TAG\": {" +
                        "      \"value\": \"release\"" +
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
                        "    }\n" +
                        "  }" +
                        "}";

        final RepositoryClient client = getRepositoryClient(configuration, metadata);
        final MavenRevision revision = client.getLatest();

        assertEquals(5, revision.getMajor());
        assertEquals(1, revision.getMinor());
        assertEquals(21, revision.getBugfix());
    }

    private RepositoryClient getRepositoryClient(final String configuration, final String meta)  throws Exception {
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        final MavenRepoConfig repoConfig = new MavenRepoConfig(configurationMessage.getRepositoryConfiguration());
        final MavenPackageConfig packageConfig = new MavenPackageConfig(configurationMessage.getPackageConfiguration(), null);

        final RepositoryConnector connector = PowerMockito.mock(RepositoryConnector.class);
        PowerMockito.whenNew(RepositoryConnector.class).withAnyArguments().thenReturn(connector);
        PowerMockito.when(connector.makeAllVersionsRequest(repoConfig, packageConfig)).thenReturn(new RepositoryResponse(meta));
        PowerMockito.when(connector.getFilesUrl(repoConfig, packageConfig, "5.1.14")).thenReturn("http://repo1.maven.org/maven2/mysql/5.1.14/mysql-connector-java-5.1.14.jar");
        PowerMockito.whenNew(RepositoryConnector.class).withAnyArguments().thenReturn(connector);
        PowerMockito.when(connector.doHttpRequest(Mockito.anyString())).thenReturn(new RepositoryResponse(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                        "    <modelVersion>4.0.0</modelVersion>\n" +
                        "\n" +
                        "    <groupId>mysql</groupId>\n" +
                        "    <artifactId>mysql-connector-java</artifactId>\n" +
                        "    <version>5.1.14</version>" +
                        "\n" +
                        "    <url>http://mysql.org</url>" +
                        "</project>"
        ));

        return new RepositoryClient(repoConfig, packageConfig);
    }
}
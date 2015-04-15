package com.oneandone.go.plugin.maven.client;

import com.oneandone.go.plugin.maven.config.MavenPackageConfig;
import com.oneandone.go.plugin.maven.config.MavenRepoConfig;
import com.oneandone.go.plugin.maven.message.ConfigurationMessage;
import com.oneandone.go.plugin.maven.util.JsonUtil;
import com.oneandone.go.plugin.maven.util.MavenArtifactFiles;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RepositoryClient.class, MavenArtifactFiles.class})
public class RepositoryClientTest {

    private String metadata;

    @Before
    public void setUp() throws Exception {
        final InputStream stream = RepositoryResponseHandlerTest.class.getClassLoader().getResourceAsStream("web/mysql/mysql-connector-java/maven-metadata.xml");
        final StringWriter writer = new StringWriter();

        IOUtils.copy(stream, writer);
        metadata = writer.toString();
    }

    @Test
    public void testGetLatest() throws Exception {
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
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(configuration, ConfigurationMessage.class);
        final MavenRepoConfig repoConfig = new MavenRepoConfig(configurationMessage.getRepositoryConfiguration());
        final MavenPackageConfig packageConfig = new MavenPackageConfig(configurationMessage.getPackageConfiguration(), null);

        final RepositoryConnector connector = PowerMockito.mock(RepositoryConnector.class);
        PowerMockito.whenNew(RepositoryConnector.class).withAnyArguments().thenReturn(connector);
        PowerMockito.when(connector.makeAllVersionsRequest(repoConfig, packageConfig)).thenReturn(new RepositoryResponse(metadata));
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

        final RepositoryClient client = new RepositoryClient(repoConfig, packageConfig);
        assertEquals(5, client.getLatest().getMajor());
        assertEquals(1, client.getLatest().getMinor());
        assertEquals(14, client.getLatest().getBugfix());
    }
}
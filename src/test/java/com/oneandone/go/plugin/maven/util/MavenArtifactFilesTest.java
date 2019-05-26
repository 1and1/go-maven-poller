package com.oneandone.go.plugin.maven.util;

import com.oneandone.go.plugin.maven.client.RepositoryConnector;
import com.oneandone.go.plugin.maven.client.RepositoryResponse;
import com.oneandone.go.plugin.maven.config.MavenRepoConfig;
import com.oneandone.go.plugin.maven.message.PackageMaterialProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MavenArtifactFiles.class)
@PowerMockIgnore({"jdk.*", "javax.xml.*", "com.sun.*", "org.w3c.*"})
public class MavenArtifactFilesTest {

    @Test
    public void testGetArtifactLocation() throws Exception {
        final MavenArtifactFiles files = new MavenArtifactFiles("http://repo1.maven.org/maven2/", "go-maven-poller-1.0.0.jar", "go-maven-poller-1.0.0.pom.xml", null);
        assertEquals("http://repo1.maven.org/maven2/go-maven-poller-1.0.0.jar", files.getArtifactLocation());
    }

    @Test
    public void testGetPomLocation() throws Exception {
        final MavenArtifactFiles files = new MavenArtifactFiles("http://repo1.maven.org/maven2/", "go-maven-poller-1.0.0.jar", "go-maven-poller-1.0.0.pom.xml", null);
        assertEquals("http://repo1.maven.org/maven2/go-maven-poller-1.0.0.pom.xml", files.getPomLocation());
    }

    @Test
    public void testGetModel() throws Exception {
        final String baseUrl = "http://repo1.maven.org/maven2/";
        final String artifact = "go-maven-poller-1.0.0.jar";
        final String pom = "go-maven-poller-1.0.0.pom.xml";

        final RepositoryConnector connector = PowerMockito.mock(RepositoryConnector.class);
        PowerMockito.whenNew(RepositoryConnector.class).withAnyArguments().thenReturn(connector);
        PowerMockito.when(connector.doHttpRequest(baseUrl + pom)).thenReturn(new RepositoryResponse(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "    <groupId>com.oneandone</groupId>\n" +
                "    <artifactId>go-maven-poller</artifactId>\n" +
                "    <version>1.0.0</version>" +
                "\n" +
                "    <url>http://www.1und1.de</url>" +
                "</project>"
        ));
        final MavenRepoConfig repoConfig = new MavenRepoConfig(new PackageMaterialProperties());
        final MavenArtifactFiles files = new MavenArtifactFiles(baseUrl, artifact, pom, repoConfig);

        assertEquals("http://www.1und1.de", files.getTrackBackUrl());
    }
}
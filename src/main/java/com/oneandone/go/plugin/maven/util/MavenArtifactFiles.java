package com.oneandone.go.plugin.maven.util;

import com.oneandone.go.plugin.maven.client.RepositoryConnector;
import com.oneandone.go.plugin.maven.client.RepositoryResponse;
import com.oneandone.go.plugin.maven.config.MavenRepoConfig;
import lombok.Getter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MavenArtifactFiles {

    @Getter private final String artifactLocation;
    @Getter private final String pomLocation;

    private final MavenRepoConfig repoConfig;
    private String url;
    private boolean modelParsed = false;

    public MavenArtifactFiles(final String baseUrlWithAuth, final String artifactLocation, final String pomLocation, final MavenRepoConfig repoConfig) {
        this.artifactLocation = baseUrlWithAuth + artifactLocation;
        this.pomLocation = baseUrlWithAuth + pomLocation;
        this.repoConfig = repoConfig;
    }

    public String getTrackBackUrl() {
        if (!modelParsed) {
            getModel();
        }
        return url;
    }

    private void getModel() {
        try {
            final RepositoryResponse repoResponse = new RepositoryConnector(repoConfig).doHttpRequest(this.getPomLocation());

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            final Document document = documentBuilder.parse(new ByteArrayInputStream(repoResponse.getResponseBody().getBytes("utf-8")));

            final Element projectElement = document.getDocumentElement();
            final NodeList urlNodes = projectElement.getElementsByTagName("url");

            if (urlNodes.getLength() > 0) {
                url = urlNodes.item(0).getTextContent();
            } else {
                url = null;
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            url = null;
        }
        modelParsed = true;
    }
}

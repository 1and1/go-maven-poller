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
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Contains the artifact locations for an artifact within a Maven repository. */
public class MavenArtifactFiles {

    /**
     * The artifact URL location.
     *
     * @return the artifact URL location
     */
    @Getter private final String artifactLocation;

    /**
     * The pom URL location.
     *
     * @return the pom URL location
     */
    @Getter private final String pomLocation;

    /** The repository configuration. */
    private final MavenRepoConfig repoConfig;

    /** The base URL with provided basic authentication information. */
    private String url;

    /** Flag indicating whether the pom was already parsed. */
    private boolean modelParsed;

    /**
     * Constructs a new maven artifact location representative.
     *
     * @param baseUrlWithAuth  he base URL with provided basic authentication information
     * @param artifactLocation the artifact location (without base)
     * @param pomLocation the pom location (without base)
     * @param repoConfig the repository configuration
     */
    public MavenArtifactFiles(final String baseUrlWithAuth, final String artifactLocation, final String pomLocation, final MavenRepoConfig repoConfig) {
        this.artifactLocation = baseUrlWithAuth + artifactLocation;
        this.pomLocation = baseUrlWithAuth + pomLocation;
        this.repoConfig = repoConfig;
    }

    /**
     * Returns the track back URL as specified by the {@code url} element of the pom.
     *
     * @return the track back URL as specified by the {@code url} element of the pom
     */
    public String getTrackBackUrl() {
        if (!modelParsed) {
            getModel();
        }
        return url;
    }

    /** Initializes the artifact model defined by the pom. */
    private void getModel() {
        try {
            final RepositoryResponse repoResponse = new RepositoryConnector(repoConfig).doHttpRequest(this.getPomLocation());

            final DocumentBuilder documentBuilder = DocumentBuilders.newDocumentBuilder();
            final Document document = documentBuilder.parse(new ByteArrayInputStream(repoResponse.getResponseBody().getBytes(StandardCharsets.UTF_8)));

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

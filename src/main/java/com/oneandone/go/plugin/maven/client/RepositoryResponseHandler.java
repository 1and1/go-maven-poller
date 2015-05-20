package com.oneandone.go.plugin.maven.client;

import com.google.common.base.Preconditions;
import com.oneandone.go.plugin.maven.exception.PluginException;
import com.oneandone.go.plugin.maven.util.MavenRevision;
import com.thoughtworks.go.plugin.api.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xml.sax.InputSource;

/** Handles the Maven repository responses in form of {@code maven-metadata.xml} contents. */
public class RepositoryResponseHandler {

    /** The logging instance for this class. */
    private static final Logger LOGGER = Logger.getLoggerFor(RepositoryResponseHandler.class);

    /** The repository response. */
    private final RepositoryResponse repoResponse;

    /** The document builder. */
    private DocumentBuilder documentBuilder;

    /** The metadata document or {@code null}. */
    private Document metaData;

    /** The XPath to the versions. */
    private XPathExpression versionsXPath;

    /** The XPath to the SNAPSHOT timestamp. */
    private XPathExpression timestampXpath;

    /** The XPath to the SNAPSHOT build number. */
    private XPathExpression buildNumberXpath;

    /**
     * Constructs a new response handler.
     *
     * @param repoResponse the Maven repository response
     */
    public RepositoryResponseHandler(final RepositoryResponse repoResponse) throws PluginException {
        this.repoResponse = repoResponse;

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            this.documentBuilder = factory.newDocumentBuilder();

            final XPathFactory xPathFactory = XPathFactory.newInstance();
            final XPath xPath = xPathFactory.newXPath();

            this.versionsXPath = xPath.compile("/metadata/versioning/versions/version");
            this.timestampXpath = xPath.compile("/metadata/versioning/snapshot/timestamp/text()");
            this.buildNumberXpath = xPath.compile("/metadata/versioning/snapshot/buildNumber/text()");
        } catch (final ParserConfigurationException | XPathExpressionException e) {
            LOGGER.error("could not create xml parsing configuration", e);
            throw new PluginException("could not initialize XML handlers", e);
        }
    }

    /**
     * Returns {@code true} if this handler can handle the repository response, otherwise {@code false}.
     *
     * @return {@code true} if this handler can handle the repository response, otherwise {@code false}
     */
    public boolean canHandle() {
        if (metaData == null) {
            try {
                metaData = documentBuilder.parse(new InputSource(new StringReader(repoResponse.getResponseBody())));
            } catch (final IOException | SAXException e) {
                LOGGER.warn("cannot handle metadata", e);
                metaData = null;
            }
        }

        return metaData != null;
    }

    /**
     * Returns all artifact versions within the metadata of the repository response.
     *
     * @return all artifact versions within the metadata of the repository response
     */
    public List<MavenRevision> getAllVersions() {
        Preconditions.checkArgument(canHandle(), "handler not initialized");

        try {
            final NodeList nodes = (NodeList) versionsXPath.evaluate(metaData, XPathConstants.NODESET);
            final int nodesLength = nodes.getLength();

            final List<MavenRevision> versions = new ArrayList<>(nodesLength);
            for (int i = 0; i < nodesLength; i++) {
                versions.add(new MavenRevision(nodes.item(i).getTextContent()));
            }
            return versions;
        } catch (final XPathExpressionException e) {
            LOGGER.error("could not get all versions by xpath", e);
            return Collections.emptyList();
        }
    }

    /**
     * Returns the Snapshot timestamp within the metadata of the repository response.
     *
     * @return the Snapshot timestamp within the metadata of the repository response
     */
    public String getSnapshotTimestamp() {
        Preconditions.checkArgument(canHandle(), "handler not initialized");
        try {
            return timestampXpath.evaluate(metaData, XPathConstants.STRING).toString();
        } catch (final XPathExpressionException e) {
            LOGGER.error("could not get timestamp value for snapshot", e);
            return null;
        }
    }

    /**
     * Returns the Snapshot build number within the metadata of the repository response.
     *
     * @return the Snapshot build number within the metadata of the repository response
     */
    public String getSnapshotBuildNumber() {
        Preconditions.checkArgument(canHandle(), "handler not initialized");
        try {
            return buildNumberXpath.evaluate(metaData, XPathConstants.STRING).toString();
        } catch (final XPathExpressionException e) {
            LOGGER.error("could not get build number value for snapshot", e);
            return null;
        }
    }
}

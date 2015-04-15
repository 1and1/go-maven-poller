package com.oneandone.go.plugin.maven.client;

import com.google.common.base.Preconditions;
import com.oneandone.go.plugin.maven.util.MavenRevision;
import com.thoughtworks.go.plugin.api.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepositoryResponseHandler {

    private static final Logger LOGGER = Logger.getLoggerFor(RepositoryResponseHandler.class);

    private final RepositoryResponse repoResponse;

    private DocumentBuilder documentBuilder;
    private Document metaData;

    private XPathExpression versionsXPath;
    private XPathExpression timestampXpath;
    private XPathExpression buildNumberXpath;


    public RepositoryResponseHandler(final RepositoryResponse repoResponse) {
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
            LOGGER.warn("could not create xml parsing configuration", e);
            this.documentBuilder = null;
        }
    }

    public boolean canHandle() {
        if (metaData == null && documentBuilder != null) {
            try {
                metaData = documentBuilder.parse(new ByteArrayInputStream(repoResponse.getResponseBody().getBytes("utf-8")));
            } catch (final IOException | SAXException e) {
                LOGGER.warn("cannot handle metadata", e);
                metaData = null;
            }
        }

        return metaData != null;
    }

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

    public String getSnapshotTimestamp() {
        Preconditions.checkArgument(canHandle(), "handler not initialized");
        try {
            return timestampXpath.evaluate(metaData, XPathConstants.STRING).toString();
        } catch (final XPathExpressionException e) {
            LOGGER.error("could not get timestamp value for snapshot", e);
            return null;
        }
    }

    public String getSnapshotBuildNumber() {
        Preconditions.checkArgument(canHandle(), "handler not initialized");
        try {
            return buildNumberXpath.evaluate(metaData, XPathConstants.STRING).toString();
        } catch (final XPathExpressionException e) {
            LOGGER.error("could not get timestamp value for snapshot", e);
            return null;
        }
    }
}

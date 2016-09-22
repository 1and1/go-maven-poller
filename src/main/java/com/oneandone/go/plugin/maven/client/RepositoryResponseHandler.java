package com.oneandone.go.plugin.maven.client;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.oneandone.go.plugin.maven.exception.PluginException;
import com.oneandone.go.plugin.maven.util.MavenRevision;
import com.thoughtworks.go.plugin.api.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/** Handles the Maven repository responses in form of {@code maven-metadata.xml} contents. */
public class RepositoryResponseHandler {

    /** The logging instance for this class. */
    private static final Logger LOGGER = Logger.getLoggerFor(RepositoryResponseHandler.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

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

    /** The XPath to the timestamp of the last update. */
    private XPathExpression lastUpdatedXpath;

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
            this.lastUpdatedXpath = xPath.compile("/metadata/versioning/lastUpdated/text()");
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
     * Returns the release version within the metadata of the repository response.
     *
     * @return the release version within the metadata of the repository response
     */
    public MavenRevision getLatestVersionByTag(final String latestVersionTag) {
        Preconditions.checkArgument(canHandle(), "handler not initialized");
        try {
            final XPathFactory xPathFactory = XPathFactory.newInstance();
            final XPath xPath = xPathFactory.newXPath();
            XPathExpression latestVersionTagXPath = xPath.compile("/metadata/versioning/" + latestVersionTag + "/text()");

            String version = latestVersionTagXPath.evaluate(metaData, XPathConstants.STRING).toString();
            if(version.isEmpty()) {
                return null;
            }
            else {
                return new MavenRevision(version);
            }
        } catch (final XPathExpressionException e) {
            LOGGER.error("could not get value for latest version tag: <" + latestVersionTag + "> by xpath", e);
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

    /**
     * Returns the optional last update date in the specified time zone.
     *
     * @param timeZone the time zone of the last update date
     * @return the optional last update date
     */
    public Optional<Date> getLastUpdated(final TimeZone timeZone) {
        Preconditions.checkArgument(canHandle(), "handler not initialized");
        try {
            final String timestamp = lastUpdatedXpath.evaluate(metaData, XPathConstants.STRING).toString();
            if (timestamp.matches("[0-9]{14}")) {
                LOGGER.info("lastUpdated set to '" + timestamp + "'");
                DATE_FORMAT.setTimeZone(timeZone);
                return Optional.of(DATE_FORMAT.parse(timestamp));
            } else {
                LOGGER.warn("lastUpdated '" + timestamp + "' does not match the expected date pattern '" + DATE_FORMAT.toPattern() + "'");
            }
        } catch (final XPathExpressionException e) {
            LOGGER.error("could not get last update value for snapshot", e);
        } catch (final ParseException e) {
            LOGGER.error("unable to parse lastUpdated", e);
        }
        return Optional.absent();
    }
}

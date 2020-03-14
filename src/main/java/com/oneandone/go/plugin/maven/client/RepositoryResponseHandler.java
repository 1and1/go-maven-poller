package com.oneandone.go.plugin.maven.client;

import com.oneandone.go.plugin.maven.GoMavenPollerException;
import com.oneandone.go.plugin.maven.util.DocumentBuilders;
import com.oneandone.go.plugin.maven.util.MavenRevision;
import com.thoughtworks.go.plugin.api.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Handles the Maven repository responses in form of {@code maven-metadata.xml} contents. */
class RepositoryResponseHandler {

    /** The logging instance for this class. */
    private static final Logger LOGGER = Logger.getLoggerFor(RepositoryResponseHandler.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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
    public RepositoryResponseHandler(final RepositoryResponse repoResponse) throws GoMavenPollerException {
        this.repoResponse = repoResponse;

        try {
            this.documentBuilder = DocumentBuilders.newDocumentBuilder();

            final XPathFactory xPathFactory = XPathFactory.newInstance();
            final XPath xPath = xPathFactory.newXPath();

            this.versionsXPath = xPath.compile("/metadata/versioning/versions/version");
            this.timestampXpath = xPath.compile("/metadata/versioning/snapshot/timestamp/text()");
            this.buildNumberXpath = xPath.compile("/metadata/versioning/snapshot/buildNumber/text()");
            this.lastUpdatedXpath = xPath.compile("/metadata/versioning/lastUpdated/text()");
        } catch (final ParserConfigurationException | XPathExpressionException e) {
            LOGGER.error("could not create xml parsing configuration", e);
            throw new GoMavenPollerException("could not initialize XML handlers", e);
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

    /** Check whether the handler is initialized.
     * @throws IllegalArgumentException if the handler is not initialized.
     * */
    private void assureCanHandle() {
        if (!canHandle()) {
            throw new IllegalArgumentException("handler not initialized");
        }
    }

    /**
     * Returns all artifact versions within the metadata of the repository response.
     *
     * @return all artifact versions within the metadata of the repository response
     */
    public List<MavenRevision> getAllVersions() {
        assureCanHandle();
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
        assureCanHandle();
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
        assureCanHandle();
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
        assureCanHandle();
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
    public Optional<ZonedDateTime> getLastUpdated(final ZoneId timeZone) {
        assureCanHandle();
        try {
            final String timestamp = lastUpdatedXpath.evaluate(metaData, XPathConstants.STRING).toString();
            if (timestamp.matches("[0-9]{14}")) {
                LOGGER.info("lastUpdated set to '" + timestamp + "'");
                final LocalDateTime localdate = LocalDateTime.parse(timestamp, DATE_FORMAT);
                return Optional.of(ZonedDateTime.of(localdate, timeZone));
            } else {
                LOGGER.warn("lastUpdated '" + timestamp + "' does not match the expected date pattern '" + DATE_FORMAT + "'");
            }
        } catch (final XPathExpressionException e) {
            LOGGER.error("could not get last update value for snapshot", e);
        }
        return Optional.empty();
    }
}

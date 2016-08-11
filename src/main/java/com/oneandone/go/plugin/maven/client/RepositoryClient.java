package com.oneandone.go.plugin.maven.client;

import com.google.common.base.Optional;
import com.oneandone.go.plugin.maven.config.MavenPackageConfig;
import com.oneandone.go.plugin.maven.config.MavenRepoConfig;
import com.oneandone.go.plugin.maven.exception.PluginException;
import com.oneandone.go.plugin.maven.util.MavenArtifactFiles;
import com.oneandone.go.plugin.maven.util.MavenRevision;
import com.thoughtworks.go.plugin.api.logging.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class RepositoryClient {

    private static final Logger LOGGER = Logger.getLoggerFor(RepositoryClient.class);

    private final RepositoryConnector repositoryConnector;
    private final MavenRepoConfig repoConfig;
    private final MavenPackageConfig packageConfig;

    public RepositoryClient(final MavenRepoConfig repoConfig, final MavenPackageConfig packageConfig) {
        this.repositoryConnector = new RepositoryConnector(repoConfig);

        this.repoConfig = repoConfig;
        this.packageConfig = packageConfig;
    }

    public MavenRevision getLatest() {
        final RepositoryResponse repoResponse = repositoryConnector.makeAllVersionsRequest(repoConfig, packageConfig);
        LOGGER.debug(repoResponse.getResponseBody());
        final List<MavenRevision> allVersions = getAllVersions(repoResponse);
        if (!allVersions.isEmpty()) {

            Optional<Date> lastUpdatedTimestamp = Optional.absent();
            try {
                final RepositoryResponseHandler repositoryResponseHandler = new RepositoryResponseHandler(repoResponse);
                if (repositoryResponseHandler.canHandle()) {
                    lastUpdatedTimestamp = repositoryResponseHandler.getLastUpdated(repoConfig.getTimeZone());
                }
            } catch (final PluginException e) {
                // do nothing here
            }


            final MavenRevision latest = getLatest(allVersions);
            if (latest != null) {
                latest.setLastModified(lastUpdatedTimestamp.or(new Date()));
                LOGGER.info("Latest is " + latest.getVersionSpecific());
                setLocationAndTrackBack(latest);
            } else {
                LOGGER.warn("getLatest returning null");
            }
            return latest;

        }

        return null;
    }

    private void setLocationAndTrackBack(final MavenRevision version) {
        try {
            final MavenArtifactFiles files = getFiles(version);
            version.setLocation(files.getArtifactLocation());
            version.setTrackBackUrl(files.getTrackBackUrl());
        } catch (final Exception ex) {
            LOGGER.error("Error getting location for " + version.getVersion(), ex);
            version.setErrorMessage("Plugin could not determine location/trackback. Please see plugin log for details.");
        }
    }

    private MavenRevision getLatest(final List<MavenRevision> allVersions) {
        if (allVersions == null || allVersions.isEmpty()) {
            return null;
        }

        final MavenRevision latest = maxSubjectToUpperBound(allVersions);
        if (latest == null) {
            LOGGER.info("maxSubjectToUpperBound is null");
            return null;
        }

        LOGGER.info("latest version is " + latest.getOriginal() + " and will be processed");

        if (latest.isSnapshot()) {
            final RepositoryResponse repositoryResponse = repositoryConnector.makeSnapshotVersionRequest(repoConfig, packageConfig, latest);
            try {
                final RepositoryResponseHandler snapshotResponseHandler = new RepositoryResponseHandler(repositoryResponse);
                if (snapshotResponseHandler.canHandle()) {
                    latest.setSnapshotInformation(snapshotResponseHandler.getSnapshotTimestamp(), snapshotResponseHandler.getSnapshotBuildNumber());
                    LOGGER.info("set snapshot information to specific version " + latest.getVersionSpecific());
                } else {
                    LOGGER.warn("could not handle snapshot resolution");
                    return null;
                }
            } catch (final PluginException e) {
                return null;
            }
        }

        if (packageConfig.isLastVersionKnown()) {
            LOGGER.info("lastKnownVersion is " + packageConfig.getLastKnownVersion());
            final MavenRevision lastKnownVersion = new MavenRevision(packageConfig.getLastKnownVersion());
            if (noNewerVersion(latest, lastKnownVersion)) {
                LOGGER.info("version " + latest.getVersionSpecific() + " is not newer than the lastKnownVersion" + lastKnownVersion.getVersionSpecific());
                return null;
            }
        }
        if (!packageConfig.lowerBoundGiven() || latest.greaterOrEqual(packageConfig.getLowerBound())) {
            return latest;
        } else {
            LOGGER.info("latestSubjectToLowerBound is null");
            return null;
        }
    }

    private MavenRevision maxSubjectToUpperBound(final List<MavenRevision> allVersions) {
        if (!packageConfig.upperBoundGiven()) {
            return Collections.max(allVersions);
        }
        Collections.sort(allVersions);
        for (int i = 0; i < allVersions.size(); i++) {
            if (allVersions.get(i).lessThan(packageConfig.getUpperBound()) && i + 1 <= allVersions.size() - 1 && allVersions.get(i + 1).greaterOrEqual(packageConfig.getUpperBound())) {
                return allVersions.get(i);
            }
            if (allVersions.get(i).lessThan(packageConfig.getUpperBound()) && i + 1 == allVersions.size()) {
                return allVersions.get(i);
            }
        }
        return null;
    }

    private boolean noNewerVersion(final MavenRevision latest, final MavenRevision lastKnownVersion) {
        return latest.notNewerThan(lastKnownVersion);
    }

    private List<MavenRevision> getAllVersions(final RepositoryResponse repoResponse) {
        try {
            final RepositoryResponseHandler repositoryResponseHandler = new RepositoryResponseHandler(repoResponse);
            if (repositoryResponseHandler.canHandle()) {
                return repositoryResponseHandler.getAllVersions();
            } else {
                LOGGER.warn("Returning empty version list - no XML nor HTML Nexus answer found");
                return Collections.emptyList();
            }
        } catch (final PluginException e) {
            return Collections.emptyList();
        }
    }

    private MavenArtifactFiles getFiles(final MavenRevision version) {
        final String baseUrl;
        if (repoConfig.getUsername() != null && !repoConfig.getUsername().isEmpty() && repoConfig.getPassword() != null && !repoConfig.getPassword().isEmpty()) {
            baseUrl = repositoryConnector.getFilesUrlWithBasicAuth(repoConfig, packageConfig, version.getOriginal());
        } else {
            baseUrl = repositoryConnector.getFilesUrl(repoConfig, packageConfig, version.getOriginal());
        }

        final String artifactFile = packageConfig.getArtifactId() + "-" + version.getVersion() + "." + packageConfig.getPackaging();
        final String pomFile = packageConfig.getArtifactId() + "-" + version.getVersion() + ".pom";

        return new MavenArtifactFiles(baseUrl, artifactFile, pomFile, repoConfig);
    }
}

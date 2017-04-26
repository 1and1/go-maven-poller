package com.oneandone.go.plugin.maven.util;

import com.oneandone.go.plugin.maven.message.PackageRevisionMessage;
import lombok.Setter;

import java.time.ZonedDateTime;

/** This revision will be wrapped in a {@link PackageRevisionMessage}. */
public class MavenRevision extends MavenVersion {

    /**
     * The URL location of the artifact for this revision.
     *
     * @param location the URL location to set
     */
    @Setter private String location;

    /**
     * The track back URL.
     *
     * @param trackBackUrl the track back URL to set
     */
    @Setter private String trackBackUrl;

    /**
     * Sets an error message for {@code this} revision.
     *
     * @param errorMessage the errorMessage
     */
    @Setter private String errorMessage;

    /**
     * The date of the last modification. Equivalent to the {@code lastUpdated} element of the {@code maven-metadata.xml}.
     *
     * @return the date of the last modification
     */
    @Setter private ZonedDateTime lastModified;

    /**
     * Constructs a new revision by the specified version.
     *
     * @param version the version
     */
    public MavenRevision(final String version) {
        super(version);
    }

    /**
     * Returns a revision message for {@code this} revision.
     *
     * @return a revision message for {@code this} revision
     */
    public PackageRevisionMessage toPackageRevision() {
        final PackageRevisionMessage packageRevision = new PackageRevisionMessage(getVersionSpecific(), lastModified, null, null, trackBackUrl);
        packageRevision.addData("LOCATION", location);
        packageRevision.addData("VERSION", getVersionSpecific());
        if (errorMessage != null) {
            packageRevision.addData("ERRORMSG", errorMessage);
        }
        return packageRevision;
    }
}

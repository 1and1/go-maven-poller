package com.oneandone.go.plugin.maven.util;

import com.oneandone.go.plugin.maven.message.PackageRevisionMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/** This revision will be wrapped in a {@link PackageRevisionMessage}. */
public class MavenRevision extends MavenVersion {

    // TODO remove this once it does not trigger a bug in Go
    private static final Date LAST_MODIFICATION_DATE = new Date(1428570532209L);

    /**
     * The URL location of the artifact for this revision.
     *
     * @param location the URL location to set
     */
    @Setter private String location = null;

    /**
     * The track back URL.
     *
     * @param trackBackUrl the track back URL to set
     */
    @Setter private String trackBackUrl = null;

    /**
     * Sets an error message for {@code this} revision.
     *
     * @param errorMessage the errorMessage
     */
    @Setter private String errorMessage;

    /**
     * The date of the last modification.
     * <p />
     * This is a static value specified in {@link #LAST_MODIFICATION_DATE} and avoids a Go CD bug. It can't be read from the {@code maven-metadata.xml}.
     *
     * @return the date of the last modification
     */
    @Getter private Date lastModified = null;

    /**
     * Constructs a new revision by the specified version.
     *
     * @param version the version
     */
    public MavenRevision(final String version) {
        super(version);
        this.lastModified = LAST_MODIFICATION_DATE;
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

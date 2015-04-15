package com.oneandone.go.plugin.maven.util;

import com.oneandone.go.plugin.maven.message.PackageRevisionMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class MavenRevision extends MavenVersion {

    // TODO remove this once it does not trigger a bug in Go
    private static final Date LAST_MODIFICATION_DATE = new Date(1428570532209L);

    @Setter private String location = null;
    @Setter private String trackBackUrl = null;
    @Setter private String errorMessage;

    @Getter private Date lastModified = null;

    public MavenRevision(final String version) {
        super(version);
        this.lastModified = LAST_MODIFICATION_DATE;
    }

    public PackageRevisionMessage toPackageRevision() {
        final PackageRevisionMessage packageRevision = new PackageRevisionMessage(getOriginal(), lastModified, null, null, trackBackUrl);
        packageRevision.addData("LOCATION", location);
        packageRevision.addData("VERSION", getVersion());
        if (errorMessage != null) {
            packageRevision.addData("ERRORMSG", errorMessage);
        }
        return packageRevision;
    }
}

package com.oneandone.go.plugin.maven.util;

import com.oneandone.go.plugin.maven.message.PackageRevisionMessage;
import org.junit.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MavenRevisionTest {

    @Test
    public void testGetRevisionLabel() throws Exception {
        final MavenRevision mavenRevision = new MavenRevision("1.2.3-beta-2");
        assertEquals("1.2.3-beta-2", mavenRevision.getVersion());
    }

    @Test
    public void testToPackageRevision() throws Exception {
        final MavenRevision mavenRevision = new MavenRevision("1.2.3-beta-2");
        mavenRevision.setErrorMessage("error msg");
        mavenRevision.setLocation("somelocation");
        mavenRevision.setTrackBackUrl("trackback");
        mavenRevision.setLastModified(ZonedDateTime.now());

        final PackageRevisionMessage packageRevision = mavenRevision.toPackageRevision();
        assertNotNull(packageRevision);

        assertNotNull(packageRevision.getTimestamp());
        assertEquals("1.2.3-beta-2", packageRevision.getRevision());
        assertEquals("trackback", packageRevision.getTrackbackUrl());
    }
}
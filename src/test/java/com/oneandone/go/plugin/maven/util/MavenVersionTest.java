package com.oneandone.go.plugin.maven.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MavenVersionTest {

    @Test(expected = NullPointerException.class)
    public void testConstructWithNull() {
        new MavenVersion(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithEmptyString() {
        new MavenVersion("");
    }

    @Test
    public void testVersionWithoutQualifier() {
        final MavenVersion version = new MavenVersion("10.2.333.4");
        assertEquals(10, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(333, version.getBugfix());
        assertEquals(4, version.getHotfix());
        assertEquals("10.2.333.4", version.getVersion());
    }

    @Test
    public void testVersionWithQualifier() {
        final MavenVersion version = new MavenVersion("10.2.333.4-sources");
        assertEquals(10, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(333, version.getBugfix());
        assertEquals(4, version.getHotfix());
        assertEquals("sources", version.getQualifier());
        assertEquals("10.2.333.4-sources", version.getVersion());
    }

    @Test
    public void testIgnoreOfLeadingOrTrailingSpaces() {
        final MavenVersion version = new MavenVersion("  10.2.333.4-sources     ");
        assertEquals(10, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(333, version.getBugfix());
        assertEquals(4, version.getHotfix());
        assertEquals("sources", version.getQualifier());
        assertEquals("10.2.333.4-sources", version.getVersion());
    }

    @Test
    public void testSort() {
        final MavenVersion a = new MavenVersion("1.2.3.4");
        final MavenVersion b = new MavenVersion("2.2.3.4");
        final MavenVersion c = new MavenVersion("0.1.2.3");
        final MavenVersion d = new MavenVersion("0.1.2.4");

        final MavenVersion[] versions = new MavenVersion[] {a, b, c, d};
        Arrays.sort(versions);

        assertEquals(c, versions[0]);
        assertEquals(d, versions[1]);
        assertEquals(a, versions[2]);
        assertEquals(b, versions[3]);
    }

    @Test
    public void testSnapshotVersion() {
        final MavenVersion version = new MavenVersion("1.2-SNAPSHOT");

        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(0, version.getBugfix());
        assertEquals(0, version.getHotfix());
        assertEquals("SNAPSHOT", version.getQualifier());
    }

    @Test
    public void testSnapshotVersionWithTimestampAndBuildNumber() {
        final MavenVersion version = new MavenVersion("1.2-SNAPSHOT (20160809.063223-25)");

        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(0, version.getBugfix());
        assertEquals(0, version.getHotfix());
        assertEquals("1.2-SNAPSHOT", version.getOriginal());
        assertEquals("1.2-20160809.063223-25", version.getVersion());
        assertEquals("1.2-SNAPSHOT (20160809.063223-25)", version.getVersionSpecific());
    }

    @Test
    public void testOrderOfSpecificSnapshotVersions() {
        final MavenVersion newer = new MavenVersion("1.2-SNAPSHOT (20160809.063223-25)");
        final MavenVersion older = new MavenVersion("1.2-SNAPSHOT (20150103.114154-25)");

        assertTrue(newer.compareTo(older) > 0);
        assertEquals(0, newer.compareTo(newer));
        assertEquals(0, older.compareTo(older));
        assertTrue(older.compareTo(newer) < 0);
    }

    @Test
    public void testComparisionOfNewAndOldVersionStyle() {
        final MavenVersion newer = new MavenVersion("1.2-SNAPSHOT (20160809.063223-25)");
        final MavenVersion older = new MavenVersion("1.2-SNAPSHOT");

        assertTrue(newer.compareTo(older) > 0);
        assertEquals(0, newer.compareTo(newer));
        assertEquals(0, older.compareTo(older));
        assertTrue(older.compareTo(newer) < 0);

        assertFalse(newer.notNewerThan(older));
        assertTrue(older.notNewerThan(older));
        assertTrue(newer.notNewerThan(newer));

        assertTrue(newer.greaterOrEqual(older));
    }

}
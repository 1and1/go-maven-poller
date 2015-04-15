package com.oneandone.go.plugin.maven.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class MavenVersionTest {

    @Test(expected = NullPointerException.class)
    public void testConstructWithNull() throws Exception {
        new MavenVersion(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithEmptyString() throws Exception {
        new MavenVersion("");
    }

    @Test
    public void testVersionWithoutQualifier() throws Exception {
        final MavenVersion version = new MavenVersion("10.2.333.4");
        assertEquals(10, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(333, version.getBugfix());
        assertEquals(4, version.getHotfix());
        assertEquals("10.2.333.4", version.getVersion());
    }

    @Test
    public void testVersionWithQualifier() throws Exception {
        final MavenVersion version = new MavenVersion("10.2.333.4-sources");
        assertEquals(10, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(333, version.getBugfix());
        assertEquals(4, version.getHotfix());
        assertEquals("sources", version.getQualifier());
        assertEquals("10.2.333.4-sources", version.getVersion());
    }

    @Test
    public void testIgnoreOfLeadingOrTrailingSpaces() throws Exception {
        final MavenVersion version = new MavenVersion("  10.2.333.4-sources     ");
        assertEquals(10, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(333, version.getBugfix());
        assertEquals(4, version.getHotfix());
        assertEquals("sources", version.getQualifier());
        assertEquals("10.2.333.4-sources", version.getVersion());
    }

    @Test
    public void testSort() throws Exception {
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
    public void testSnapshotVersion() throws Exception {
        final MavenVersion version = new MavenVersion("1.2-SNAPSHOT");

        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(0, version.getBugfix());
        assertEquals(0, version.getHotfix());
        assertEquals("SNAPSHOT", version.getQualifier());
    }

}
package com.oneandone.go.plugin.maven.client;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link RepositoryConnector}.
 */
public class RepositoryConnectorTest {

    @Test
    public void testConcatUrl() {
        String url = RepositoryConnector.concatUrl("http://www.test.org/", "foo", "bar", "1.0");
        assertEquals("http://www.test.org/foo/bar/1.0/", url);
    }

    @Test
    public void testConcatUrlWithMissingBaseUrlSlash() {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo", "bar", "1.0");
        assertEquals("http://www.test.org/foo/bar/1.0/", url);
    }

    @Test
    public void testConcatUrlWithPointInGroupId() {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }

    @Test
    public void testConcatUrlWithSlashBeforeGroupId() {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "/foo.com", "bar", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }

    @Test
    public void testConcatUrlWithSlashAfterGroupId() {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com/", "bar", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }

    @Test
    public void testConcatUrlWithSlashBeforeArtifactId() {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "/bar", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }

    @Test
    public void testConcatUrlWithSlashAfterArtifactId() {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar/", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }

    @Test
    public void testConcatUrlWithSlashBeforeVersion() {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar", "/1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }

    @Test
    public void testConcatUrlWithSlashAfterVersion() {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar", "1.0/");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }

    @Test
    public void testConcatUrlWithNullVersion() {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar/", null);
        assertEquals("http://www.test.org/foo/com/bar/maven-metadata.xml", url);
    }

}

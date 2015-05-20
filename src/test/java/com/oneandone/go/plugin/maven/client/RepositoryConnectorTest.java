package com.oneandone.go.plugin.maven.client;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test case for {@link RepositoryConnector}.
 */
public class RepositoryConnectorTest {

    @Test
    public void testConcatUrl() throws Exception {
        String url = RepositoryConnector.concatUrl("http://www.test.org/", "foo", "bar", "1.0");
        assertEquals("http://www.test.org/foo/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithMissingBaseUrlSlash() throws Exception {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo", "bar", "1.0");
        assertEquals("http://www.test.org/foo/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithPointInGroupId() throws Exception {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithSlashBeforeGroupId() throws Exception {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "/foo.com", "bar", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithSlashAfterGroupId() throws Exception {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com/", "bar", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithSlashBeforeArtifactId() throws Exception {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "/bar", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithSlashAfterArtifactId() throws Exception {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar/", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithSlashBeforeVersion() throws Exception {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar", "/1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithSlashAfterVersion() throws Exception {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar", "1.0/");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithNullVersion() throws Exception {
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar/", null);
        assertEquals("http://www.test.org/foo/com/bar/maven-metadata.xml", url);
    }
    
}

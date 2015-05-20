package com.oneandone.go.plugin.maven.client;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Test case for {@link RepositoryConnector}.
 */
public class RepositoryConnectorTest extends TestCase {
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
        // TODO this special case looks very very buggy
        String url = RepositoryConnector.concatUrl("http://www.test.org", "/foo.com", "bar", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithSlashAfterGroupId() throws Exception {
        // TODO this special case looks very very buggy
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com/", "bar", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithSlashBeforeArtifactId() throws Exception {
        // TODO this special case looks very very buggy
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "/bar", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithSlashAfterArtifactId() throws Exception {
        // TODO this special case looks very very buggy
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar/", "1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithSlashBeforeVersion() throws Exception {
        // TODO this special case looks very very buggy
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar", "/1.0");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithSlashAfterVersion() throws Exception {
        // TODO this special case looks very very buggy
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar", "1.0/");
        assertEquals("http://www.test.org/foo/com/bar/1.0/", url);
    }
    
    @Test
    public void testConcatUrlWithNullVersion() throws Exception {
        // TODO this special case looks very very buggy
        String url = RepositoryConnector.concatUrl("http://www.test.org", "foo.com", "bar/", null);
        assertEquals("http://www.test.org/foo/com/bar/maven-metadata.xml", url);
    }
    
}

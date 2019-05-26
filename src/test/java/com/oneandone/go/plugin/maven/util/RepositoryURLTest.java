package com.oneandone.go.plugin.maven.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class RepositoryURLTest {

    @Test
    public void testHasCredentials() {
        RepositoryURL url = new RepositoryURL("http://localhost/", "admin", "12345");
        assertTrue(url.hasCredentials());

        url = new RepositoryURL("http://localhost/", null, null);
        assertFalse(url.hasCredentials());
    }

    @Test
    public void testGetURL() {
        final RepositoryURL url = new RepositoryURL("http://localhost/repo", "admin", "12345");
        assertEquals("http://localhost/repo/", url.getURL());
    }

    @Test
    public void testGetURLWithBasicAuth() {
        final RepositoryURL url = new RepositoryURL("http://localhost/repo", "admin", "12345");
        assertEquals("http://admin:12345@localhost/repo/", url.getURLWithBasicAuth());
    }

    @Test
    public void testIsHttp() {
        RepositoryURL url = new RepositoryURL("http://localhost/", null, null);
        assertTrue(url.isHttp());

        url = new RepositoryURL("https://localhost/", null, null);
        assertTrue(url.isHttp());

        url = new RepositoryURL("ftp://localhost/", null, null);
        assertFalse(url.isHttp());
    }
}
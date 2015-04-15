package com.oneandone.go.plugin.maven.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/** Representation of a Maven repository URL. */
public class RepositoryURL {

    /** The URL. */
    private final URL url;

    /** The username or {@code null}. */
    private final String username;

    /** The password or {@code null}. */
    private final String password;

    /**
     * Constructs a new repository url.
     *
     * @param url the url
     * @param username the authentication username
     * @param password the authentication password
     * @throws IllegalArgumentException if the specified url is malformed
     */
    public RepositoryURL(final String url, final String username, final String password) {
        try {
            this.url = new URL(url);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        this.username = username;
        this.password = password;
    }

    /**
     * Returns {@code true} if credentials are provided for this repository, otherwise {@code false}.
     *
     * @return {@code true} if credentials are provided for this repository, otherwise {@code false}
     */
    public boolean hasCredentials() {
        return username != null && password != null;
    }

    /**
     * Returns the URL with a trailing slash.
     *
     * @return the URL with a trailing slash
     */
    public String getURL() {
        return url.toString().endsWith("/") ? url.toString() : url.toString() + "/";
    }

    /**
     * Returns the URL with a trailing slash and the basic authentication information.
     *
     * @return the URL with a trailing slash and the basic authentication information
     */
    public String getURLWithBasicAuth() {
        final StringBuilder sb = new StringBuilder();
        sb.append(url.getProtocol());
        sb.append("://");

        if (hasCredentials()) {
            try {
                sb.append(String.format("%s:%s", this.username, URLEncoder.encode(this.password, "UTF-8"))).append("@");
            } catch (final UnsupportedEncodingException e) {
                // should not happen
            }
        }

        sb.append(url.getHost());
        if (url.getPort() != -1) {
            sb.append(":").append(url.getPort());
        }

        sb.append(url.getPath());
        if (url.getQuery() != null) {
            sb.append("?").append(url.getQuery());
        }

        if (url.getRef() != null) {
            sb.append("#").append(url.getRef());
        }

        if (url.getQuery() == null && url.getRef() == null && !url.getPath().endsWith("/")) {
            sb.append("/");
        }
        return sb.toString();
    }

    /**
     * Returns {@code true} if this URL uses either the {@code http(s)} protocol, otherwise {@code false}.
     *
     * @return {@code true} if this URL uses either the {@code http(s)} protocol, otherwise {@code false}
     */
    public boolean isHttp() {
        return "http".equals(url.getProtocol().toLowerCase()) || "https".equals(url.getProtocol().toLowerCase());
    }
}

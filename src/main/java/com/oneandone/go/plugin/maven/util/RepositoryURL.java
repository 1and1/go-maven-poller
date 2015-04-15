package com.oneandone.go.plugin.maven.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class RepositoryURL {

    private final URL url;
    private final String username;
    private final String password;

    public RepositoryURL(final String url, final String username, final String password) {
        try {
            this.url = new URL(url);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        this.username = username;
        this.password = password;
    }

    public boolean hasCredentials() {
        return username != null && password != null;
    }

    public String getURL() {
        return url.toString().endsWith("/") ? url.toString() : url.toString() + "/";
    }

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

    public boolean isHttp() {
        return "http".equals(url.getProtocol().toLowerCase()) || "https".equals(url.getProtocol().toLowerCase());
    }
}

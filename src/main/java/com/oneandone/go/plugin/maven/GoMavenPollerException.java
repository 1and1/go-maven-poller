package com.oneandone.go.plugin.maven;

/**
 * Application wide runtime exception.
 */
public class GoMavenPollerException extends RuntimeException {
    public GoMavenPollerException(String message) {
        super(message);
    }

    public GoMavenPollerException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoMavenPollerException(Throwable cause) {
        super(cause);
    }
}

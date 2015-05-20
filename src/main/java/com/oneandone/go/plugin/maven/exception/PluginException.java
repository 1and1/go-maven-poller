package com.oneandone.go.plugin.maven.exception;

public class PluginException extends Exception {

    private static final long serialVersionUID = 1L;

    public PluginException(final String detailMessage, final Throwable cause) {
        super(detailMessage, cause);
    }

}

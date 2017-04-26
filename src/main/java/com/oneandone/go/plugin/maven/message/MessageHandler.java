package com.oneandone.go.plugin.maven.message;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

/** This is an interface for a Go CD server message handler. */
public interface MessageHandler {

    /**
     * Handles the request and returns a response.
     *
     * @param request the request to handle
     * @return the response
     */
    GoPluginApiResponse handle(final GoPluginApiRequest request);
}

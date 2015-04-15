package com.oneandone.go.plugin.maven.client;

import lombok.Getter;

/**
 * Representation of a Maven repository response
 * <p />
 * This class can be extended if e.g. the MIME type should be analyzed.
 */
public class RepositoryResponse {

    /**
     * The response body.
     *
     * @return the response body
     */
    @Getter private final String responseBody;

    /**
     * Constructs a Maven repository response by the specified repsonse body.
     *
     * @param responseBody the response body
     */
    public RepositoryResponse(final String responseBody) {
        this.responseBody = responseBody;
    }
}

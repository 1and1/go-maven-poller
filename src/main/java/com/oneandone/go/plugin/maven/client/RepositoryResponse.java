package com.oneandone.go.plugin.maven.client;

import lombok.Getter;

public class RepositoryResponse {

    @Getter private final String responseBody;

    public RepositoryResponse(final String responseBody) {
        this.responseBody = responseBody;
    }
}

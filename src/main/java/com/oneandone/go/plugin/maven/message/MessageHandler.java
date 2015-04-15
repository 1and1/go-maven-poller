package com.oneandone.go.plugin.maven.message;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

public interface MessageHandler {
    GoPluginApiResponse handle(GoPluginApiRequest request);
}

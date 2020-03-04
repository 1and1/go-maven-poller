package com.oneandone.go.plugin.maven;

import com.oneandone.go.plugin.maven.config.ConfigurationProvider;
import com.oneandone.go.plugin.maven.message.*;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.oneandone.go.plugin.maven.util.JsonUtil.fromJsonString;
import static com.oneandone.go.plugin.maven.util.JsonUtil.toJsonString;
import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.success;

/**
 * The Go CD Maven repository plugin.
 */
@Extension
public class MavenRepositoryMaterial implements GoPlugin {

    /** The logging instance for this class. */
    private static final Logger LOGGER = Logger.getLoggerFor(MavenRepositoryMaterial.class);

    /** The plugin extension type. */
    public static final String EXTENSION = "package-repository";

    /** Request to retrieve plugin configuration.
     * @see #getConfigurationMessageHandler()
     * */
    public static final String REQUEST_PLUGIN_GET_CONFIGURATION = "go.plugin-settings.get-configuration";

    /** Request to retrieve the repository configuration definition.
     * @see #repositoryConfigurationsMessageHandler()
     * */
    public static final String REQUEST_REPOSITORY_CONFIGURATION = "repository-configuration";

    /** Request to retrieve the package configuration definition.
     * @see #packageConfigurationMessageHandler()
     * */
    public static final String REQUEST_PACKAGE_CONFIGURATION = "package-configuration";

    /** Request to validate the repository configuration.
     * @see #validateRepositoryConfigurationMessageHandler()
     * */
    public static final String REQUEST_VALIDATE_REPOSITORY_CONFIGURATION = "validate-repository-configuration";

    /** Request to validate the package configuration.
     * @see #validatePackageConfigurationMessageHandler()
     * */
    public static final String REQUEST_VALIDATE_PACKAGE_CONFIGURATION = "validate-package-configuration";

    /** Request to check the repository connection.
     * @see #checkRepositoryConnectionMessageHandler()
     * */
    public static final String REQUEST_CHECK_REPOSITORY_CONNECTION = "check-repository-connection";

    /** Request to check the package connection.
     * @see #checkPackageConnectionMessageHandler()
     * */
    public static final String REQUEST_CHECK_PACKAGE_CONNECTION = "check-package-connection";

    /** Request to retrieve the latest revision.
     * @see #latestRevisionMessageHandler()
     * */
    public static final String REQUEST_LATEST_PACKAGE_REVISION = "latest-revision";

    /** Request to retrieve the latest revision since a specified revision.
     * @see #latestRevisionSinceMessageHandler()
     * */
    public static final String REQUEST_LATEST_PACKAGE_REVISION_SINCE = "latest-revision-since";

    /** The map of message handlers. */
    private final Map<String, MessageHandler> handlerMap = new LinkedHashMap<>();

    /** The configuration provider for this plugin. */
    private final ConfigurationProvider configurationProvider;

    /** The repository poller analyzes the contents of a repository and retrieves the latest revision. */
    private final MavenRepositoryPoller packageRepositoryPoller;

    /** The go application accessor. */
    private GoApplicationAccessor goApplicationAccessor;

    /** Constructs this plugin and initializes the message handlers. */
    public MavenRepositoryMaterial() {
        configurationProvider = new ConfigurationProvider();
        packageRepositoryPoller = new MavenRepositoryPoller();

        handlerMap.put(REQUEST_PLUGIN_GET_CONFIGURATION, getConfigurationMessageHandler());
        handlerMap.put(REQUEST_REPOSITORY_CONFIGURATION, repositoryConfigurationsMessageHandler());
        handlerMap.put(REQUEST_PACKAGE_CONFIGURATION, packageConfigurationMessageHandler());
        handlerMap.put(REQUEST_VALIDATE_REPOSITORY_CONFIGURATION, validateRepositoryConfigurationMessageHandler());
        handlerMap.put(REQUEST_VALIDATE_PACKAGE_CONFIGURATION, validatePackageConfigurationMessageHandler());
        handlerMap.put(REQUEST_CHECK_REPOSITORY_CONNECTION, checkRepositoryConnectionMessageHandler());
        handlerMap.put(REQUEST_CHECK_PACKAGE_CONNECTION, checkPackageConnectionMessageHandler());
        handlerMap.put(REQUEST_LATEST_PACKAGE_REVISION, latestRevisionMessageHandler());
        handlerMap.put(REQUEST_LATEST_PACKAGE_REVISION_SINCE, latestRevisionSinceMessageHandler());
    }

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.goApplicationAccessor = goApplicationAccessor;
    }

    @Override
    public GoPluginApiResponse handle(final GoPluginApiRequest goPluginApiRequest) {
        try {
            if (handlerMap.containsKey(goPluginApiRequest.requestName())) {
                return handlerMap.get(goPluginApiRequest.requestName()).handle(goPluginApiRequest);
            }
            return DefaultGoPluginApiResponse.badRequest(String.format("Invalid request name %s", goPluginApiRequest.requestName()));
        } catch (final Throwable e) {
            LOGGER.error("could not handle request with name \"" + goPluginApiRequest.requestName() + "\" and body: " + goPluginApiRequest.requestBody(), e);
            return DefaultGoPluginApiResponse.error(e.getMessage());
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION, Collections.singletonList("1.0"));
    }

    private MessageHandler getConfigurationMessageHandler() {
        return request -> success(toJsonString(new Object()));
    }

    /**
     * Returns a message handler for request of type {@link #REQUEST_PACKAGE_CONFIGURATION}.
     *
     * @return the message handler
     */
    private MessageHandler packageConfigurationMessageHandler() {
        return request -> success(toJsonString(configurationProvider.getPackageConfiguration().getPropertyMap()));
    }

    /**
     * Returns a message handler for request of type {@link #REQUEST_REPOSITORY_CONFIGURATION}.
     *
     * @return the message handler
     */
    private MessageHandler repositoryConfigurationsMessageHandler() {
        return request -> success(toJsonString(configurationProvider.getRepositoryConfiguration().getPropertyMap()));
    }

    /**
     * Returns a message handler for request of type {@link #REQUEST_VALIDATE_REPOSITORY_CONFIGURATION}.
     *
     * @return the message handler
     */
    private MessageHandler validateRepositoryConfigurationMessageHandler() {
        return request -> {
            final ConfigurationMessage message = fromJsonString(request.requestBody(), ConfigurationMessage.class);
            final ValidationResultMessage validationResultMessage = configurationProvider.isRepositoryConfigurationValid(message.getRepositoryConfiguration());
            if (validationResultMessage.failure()) {
                return success(toJsonString(validationResultMessage.getValidationErrors()));
            }
            return success("");
        };
    }

    /**
     * Returns a message handler for request of type {@link #REQUEST_VALIDATE_PACKAGE_CONFIGURATION}.
     *
     * @return the message handler
     */
    private MessageHandler validatePackageConfigurationMessageHandler() {
        return request -> {
            final ConfigurationMessage message = fromJsonString(request.requestBody(), ConfigurationMessage.class);
            final ValidationResultMessage validationResultMessage = configurationProvider.isPackageConfigurationValid(message.getPackageConfiguration());
            if (validationResultMessage.failure()) {
                return success(toJsonString(validationResultMessage.getValidationErrors()));
            }
            return success("");
        };
    }

    /**
     * Returns a message handler for request of type {@link #REQUEST_CHECK_REPOSITORY_CONNECTION}.
     *
     * @return the message handler
     */
    private MessageHandler checkRepositoryConnectionMessageHandler() {
        return request -> {
            final ConfigurationMessage message = fromJsonString(request.requestBody(), ConfigurationMessage.class);
            final CheckConnectionResultMessage result = packageRepositoryPoller.checkConnectionToRepository(message.getRepositoryConfiguration());
            return success(toJsonString(result));
        };
    }

    /**
     * Returns a message handler for request of type {@link #REQUEST_CHECK_PACKAGE_CONNECTION}.
     *
     * @return the message handler
     */
    private MessageHandler checkPackageConnectionMessageHandler() {
        return request -> {
            final ConfigurationMessage message = fromJsonString(request.requestBody(), ConfigurationMessage.class);
            final CheckConnectionResultMessage result = packageRepositoryPoller.checkConnectionToPackage(message.getPackageConfiguration(), message.getRepositoryConfiguration());
            return success(toJsonString(result));
        };
    }

    /**
     * Returns a message handler for request of type {@link #REQUEST_LATEST_PACKAGE_REVISION}.
     *
     * @return the message handler
     */
    private MessageHandler latestRevisionMessageHandler() {
        return request -> {
            final ConfigurationMessage message = fromJsonString(request.requestBody(), ConfigurationMessage.class);
            final PackageRevisionMessage revision = packageRepositoryPoller.getLatestRevision(message.getPackageConfiguration(), message.getRepositoryConfiguration());
            return success(toJsonString(revision));
        };
    }

    /**
     * Returns a message handler for request of type {@link #REQUEST_LATEST_PACKAGE_REVISION_SINCE}.
     *
     * @return the message handler
     */
    private MessageHandler latestRevisionSinceMessageHandler() {
        return request -> {
            final ConfigurationMessage message = fromJsonString(request.requestBody(), ConfigurationMessage.class);
            final PackageRevisionMessage revision = packageRepositoryPoller.latestModificationSince(message.getPackageConfiguration(), message.getRepositoryConfiguration(), message.getPreviousRevision());
            return success(revision == null ? null : toJsonString(revision));
        };
    }

}

package com.oneandone.go.plugin.maven.client;

import com.oneandone.go.plugin.maven.config.MavenPackageConfig;
import com.oneandone.go.plugin.maven.config.MavenRepoConfig;
import com.oneandone.go.plugin.maven.util.MavenVersion;
import com.thoughtworks.go.plugin.api.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/** The Maven repository connector. */
public class RepositoryConnector {

    /** The logging instance for this class. */
    private static final Logger LOGGER = Logger.getLoggerFor(RepositoryConnector.class);

    /** The repository configuration. */
    private final MavenRepoConfig repoConfig;

    /**
     * Constructs a connector by the specified configuration.
     *
     * @param repoConfig the repository configuration
     */
    public RepositoryConnector(final MavenRepoConfig repoConfig) {
        this.repoConfig = repoConfig;
    }

    static String concatUrl(final String baseUrl, final String groupId, final String artifactId, final String version) {
        try {
            final URL base = new URL(baseUrl);
            URL url;

            if (version != null) {
                url = new URL(base, 
                    filterSlash(groupId).replaceAll("\\.", "/")+"/"+
                    filterSlash(artifactId)+"/"+
                    (version.isEmpty() ? "" : filterSlash(version)+"/"));
            } else {
                url = new URL(base, 
                    filterSlash(groupId).replaceAll("\\.", "/")+"/"+
                    filterSlash(artifactId)+"/"+
                    "maven-metadata.xml");
            }
            return url.toExternalForm();
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /** Removes slash parts. */
    private static String filterSlash(String in) {
        return in.contains("/") ? in.replaceAll("/", "") : in;
    }

    /**
     * Executes a HTTP {@code GET} on the specified url and returns the response.
     * <p />
     * The connection will be released after the operation.
     *
     * @param url the URL
     * @return the response
     * @throws RuntimeException on any exception
     */
    public RepositoryResponse doHttpRequest(final String url) {
        final HttpClient client = createHttpClient();

        String responseBody;
        HttpGet method = null;
        try {
            method = createGetMethod(url, true);
            HttpResponse response = client.execute(method);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException(String.format("HTTP %s, %s", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }
            HttpEntity entity = response.getEntity();
            responseBody = EntityUtils.toString(entity);
            return new RepositoryResponse(responseBody);
        } catch (final Exception e) {
            String message = String.format("Exception while connecting to %s\n%s", url, e);
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    /**
     * Creates a HTTP {@code GET} on the specified url.
     *
     * @param url the URL
     * @param expectXml flag indicating whet xml is expected to be xml formatted
     * @return the HTTP {@code GET} operation
     */
    private HttpGet createGetMethod(final String url, final boolean expectXml) {
        final HttpGet method = new HttpGet(url);
        method.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10 * 1000);
        if (expectXml) {
            method.setHeader("Accept", "application/xml");
        }
        if (repoConfig.getProxy() != null) {
            method.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, HttpHost.create(repoConfig.getProxy()));
        }
        return method;
    }

    /**
     * Returns a new HTTP client by the specified repository configuration.
     *
     * @return a new HTTP client by the specified repository configuration
     */
    private HttpClient createHttpClient() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder = httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(3, false));
        httpClientBuilder = httpClientBuilder.setRedirectStrategy(new DefaultRedirectStrategy());

        if (repoConfig.getUsername() != null) {
            final Credentials creds = new UsernamePasswordCredentials(repoConfig.getUsername(), repoConfig.getPassword());
            final CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, creds);
            httpClientBuilder = httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
        }
        return httpClientBuilder.build();
    }

    /**
     * Tests the connection to the base URL of the repository, returns {@code true} on success and {@code false} otherwise.
     *
     * @return {@code true} if a connection could be established, otherwise {@code false}
     * @throws RuntimeException on any exception
     */
    public boolean testConnection() {
        final String url = repoConfig.getRepoUrlAsString();
        boolean result = false;
        final HttpClient client = createHttpClient();

        HttpGet method = null;
        try {
            method = createGetMethod(url, false);

            final HttpResponse response = client.execute(method);
            result = (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);

            if (!result) {
                final StringBuilder builder = new StringBuilder();
                if (response.getEntity() != null) {
                    try (final BufferedReader bReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                        String line;
                        while ((line = bReader.readLine()) != null) {
                            builder.append(line);
                        }
                    }
                }

                if (builder.length() == 0) {
                    LOGGER.error(String.format("expected HTTP status 200 but got %d on check of url '%s'", response.getStatusLine().getStatusCode(), url));
                } else {
                    LOGGER.error(String.format(
                            "expected HTTP status 200 but got %d on check of url '%s', with entity: %s",
                            response.getStatusLine().getStatusCode(),
                            url,
                            builder.toString()
                    ));
                }
            }
        } catch (final Exception e) {
            final String message = String.format("Exception while connecting to %s\n%s", url, e.getMessage());
            LOGGER.error(message);
            throw new RuntimeException(message, e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
        return result;
    }

    /**
     * Constructs and executes a snapshot version request.
     *
     * @param repoConfig the repository configuration
     * @param packageConfig the package configuration
     * @param version the snapshot version to extend with {@link MavenVersion#setSnapshotInformation(String, String)}
     * @return the repository response
     */
    public RepositoryResponse makeSnapshotVersionRequest(final MavenRepoConfig repoConfig, final MavenPackageConfig packageConfig, final MavenVersion version) {
        final String url = concatUrl(repoConfig.getRepoUrlAsString(), packageConfig.getGroupId(), packageConfig.getArtifactId(), version.toString()) + "maven-metadata.xml";
        LOGGER.info("Getting version for SNAPSHOT " + url);
        return doHttpRequest(url);
    }

    /**
     * Constructs and executes a request for all versions.
     *
     * @param repoConfig the repository configuration
     * @param packageConfig the package configuration
     * @return the repository response
     */
    public RepositoryResponse makeAllVersionsRequest(final MavenRepoConfig repoConfig, final MavenPackageConfig packageConfig) {
        final String url = concatUrl(repoConfig.getRepoUrlAsString(), packageConfig.getGroupId(), packageConfig.getArtifactId(), null);
        LOGGER.info("Getting versions from " + url);
        return doHttpRequest(url);
    }

    /**
     * Returns the URL for the specified revision.
     *
     * @param repoConfig the repository configuration
     * @param packageConfig the package configuration
     * @param revision the revision
     * @return the URL
     */
    public String getFilesUrl(final MavenRepoConfig repoConfig, final MavenPackageConfig packageConfig, final String revision) {
        return concatUrl(repoConfig.getRepoUrlAsString(), packageConfig.getGroupId(), packageConfig.getArtifactId(), revision);
    }

    /**
     * Returns the URL with basic authentication information for the specified revision.
     *
     * @param repoConfig the repository configuration
     * @param packageConfig the package configuration
     * @param revision the revision
     * @return the URL with basic authentication information
     */
    public String getFilesUrlWithBasicAuth(final MavenRepoConfig repoConfig, final MavenPackageConfig packageConfig, final String revision) {
        return concatUrl(repoConfig.getRepoUrlAsStringWithBasicAuth(), packageConfig.getGroupId(), packageConfig.getArtifactId(), revision);
    }
}
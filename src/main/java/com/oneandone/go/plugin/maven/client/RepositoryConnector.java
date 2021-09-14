package com.oneandone.go.plugin.maven.client;

import com.oneandone.go.plugin.maven.GoMavenPollerException;
import com.oneandone.go.plugin.maven.config.MavenPackageConfig;
import com.oneandone.go.plugin.maven.config.MavenRepoConfig;
import com.oneandone.go.plugin.maven.util.MavenVersion;
import com.thoughtworks.go.plugin.api.logging.Logger;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
            throw new GoMavenPollerException(ex);
        }
    }

    /** Removes slash parts. */
    private static String filterSlash(String in) {
        return in.contains("/") ? in.replaceAll("/", "") : in;
    }

    /**
     * Executes a HTTP {@code GET} on the specified url and returns the response.
     * <br>
     * The connection will be released after the operation.
     *
     * @param url the URL
     * @return the response
     * @throws RuntimeException on any exception
     */
    public RepositoryResponse doHttpRequest(final String url) {
        String responseBody;
        try (final CloseableHttpClient client = createHttpClient()) {
            HttpGet method = new HttpGet(url);
            method.setHeader(HttpHeaders.ACCEPT, "application/xml");
            try (CloseableHttpResponse response = client.execute(method)) {
                if (response.getCode() != HttpStatus.SC_OK) {
                    throw new GoMavenPollerException(String.format("HTTP %s, %s", response.getCode(), response.getReasonPhrase()));
                }
                HttpEntity entity = response.getEntity();
                responseBody = EntityUtils.toString(entity);
                return new RepositoryResponse(responseBody);
            }
        } catch (final Exception e) {
            String message = String.format("Exception while connecting to %s%n%s", url, e);
            LOGGER.error(message, e);
            throw new GoMavenPollerException(message, e);
        }
    }

    /**
     * Returns a new HTTP client by the specified repository configuration.
     *
     * @return a new HTTP client by the specified repository configuration
     */
    private CloseableHttpClient createHttpClient() throws URISyntaxException {
        RequestConfig.Builder requestBuilder = RequestConfig.custom().setConnectTimeout(Timeout.ofSeconds(10));

        if (repoConfig.getProxy() != null) {
            requestBuilder.setProxy(HttpHost.create(repoConfig.getProxy()));
        }

        HttpClientBuilder httpClientBuilder =
                HttpClientBuilder.create()
                        .setDefaultRequestConfig(requestBuilder.build())
                        .setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.ofSeconds(2)))
                        .setRedirectStrategy(new DefaultRedirectStrategy());

        if (repoConfig.getUsername() != null) {
            final Credentials credentials = new UsernamePasswordCredentials(repoConfig.getUsername(), repoConfig.getPassword().toCharArray());
            final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            final URI repoUri = repoConfig.getRepoUrlAsURI();
            credentialsProvider.setCredentials(new AuthScope(repoUri.getHost(), repoUri.getPort()), credentials);
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
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
        final URI uri = repoConfig.getRepoUrlAsURI();
        //noinspection UnusedAssignment
        boolean result = false;

        try (final CloseableHttpClient client = createHttpClient()) {
            // try with HTTP HEAD
            HttpUriRequestBase headRequest = new HttpHead(uri);
            headRequest.setHeader(HttpHeaders.ACCEPT, "*/*");
            try (CloseableHttpResponse response = client.execute(headRequest)) {
                result = (response.getCode() == HttpStatus.SC_OK);
            }

            if (!result) {
                LOGGER.warn("http HEAD failed for repository '" + uri.toASCIIString() + "' will proceed with GET request");
                HttpUriRequestBase getRequest = new HttpGet(uri);
                getRequest.setHeader(HttpHeaders.ACCEPT, "*/*");
                try (CloseableHttpResponse response = client.execute(getRequest)) {
                    result = (response.getCode() == HttpStatus.SC_OK);

                    if (!result) {
                        final StringBuilder builder = new StringBuilder();
                        if (response.getEntity() != null) {
                            try (final BufferedReader bReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = bReader.readLine()) != null) {
                                    builder.append(line);
                                }
                            }
                        }

                        if (builder.length() == 0) {
                            LOGGER.error(String.format("expected HTTP status 200 but got %d on check of url '%s'", response.getCode(), uri.toASCIIString()));
                        } else {
                            LOGGER.error(String.format(
                                    "expected HTTP status 200 but got %d on check of url '%s', with entity: %s",
                                    response.getCode(),
                                    uri.toASCIIString(),
                                    builder
                            ));
                        }
                    }
                } //
            }

        } catch (final Exception e) {
            final String message = String.format("Exception while connecting to %s%n%s", uri.toASCIIString(), e.getMessage());
            LOGGER.error(message);
            throw new GoMavenPollerException(message, e);
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
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
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

public class RepositoryConnector {

    private static final Logger LOGGER = Logger.getLoggerFor(RepositoryConnector.class);

    private final MavenRepoConfig repoConfig;

    public RepositoryConnector(final MavenRepoConfig repoConfig) {
        this.repoConfig = repoConfig;
    }

    public static String concatUrl(final String baseUrl, final String groupId, final String artifactId, final String version) {
        final StringBuilder sb = new StringBuilder();
        sb.append(baseUrl);
        if (!baseUrl.endsWith("/")) {
            sb.append("/");
        }
        if (groupId.startsWith("/")) {
            sb.append(groupId.substring(1).replace('.', '/'));
        } else {
            sb.append(groupId.replace('.', '/'));
        }
        if (!groupId.endsWith("/")) {
            sb.append("/");
        }
        if (artifactId.startsWith("/")) {
            sb.append(artifactId.substring(1));
        } else {
            sb.append(artifactId);
        }
        if (!artifactId.endsWith("/")) {
            sb.append("/");
        }
        if (version == null) {
            sb.append("maven-metadata.xml");
        } else {
            if (version.startsWith("/")) {
                sb.append(version.substring(1));
            } else {
                sb.append(version);
            }
            if (!version.isEmpty() && !version.endsWith("/")) {
                sb.append("/");
            }
        }
        return sb.toString();
    }

    public RepositoryResponse doHttpRequest(final String url) {
        final HttpClient client = createHttpClient();

        String responseBody;
        HttpGet method = null;
        try {
            method = createGetMethod(url);
            HttpResponse response = client.execute(method);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException(String.format("HTTP %s, %s", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }
            HttpEntity entity = response.getEntity();
            responseBody = EntityUtils.toString(entity);
            String mimeType = ContentType.get(entity).getMimeType();
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

    private HttpGet createGetMethod(final String url) {
        final HttpGet method = new HttpGet(url);
        method.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10 * 1000);
        method.setHeader("Accept", "application/xml");
        if (repoConfig.getProxy() != null) {
            method.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, HttpHost.create(repoConfig.getProxy()));
        }
        return method;
    }

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

    public boolean testConnection() {
        final String url = repoConfig.getRepoUrlAsString();
        boolean result = false;
        final HttpClient client = createHttpClient();

        HttpGet method = null;
        try {
            method = createGetMethod(url);

            final HttpResponse response = client.execute(method);
            result = (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
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

    public RepositoryResponse makeSnapshotVersionRequest(final MavenRepoConfig repoConfig, final MavenPackageConfig packageConfig, final MavenVersion version) {
        final String url = concatUrl(repoConfig.getRepoUrlAsString(), packageConfig.getGroupId(), packageConfig.getArtifactId(), version.toString()) + "maven-metadata.xml";
        LOGGER.info("Getting version for SNAPSHOT " + url);
        return doHttpRequest(url);
    }

    public RepositoryResponse makeAllVersionsRequest(final MavenRepoConfig repoConfig, final MavenPackageConfig packageConfig) {
        final String url = concatUrl(repoConfig.getRepoUrlAsString(), packageConfig.getGroupId(), packageConfig.getArtifactId(), null);
        LOGGER.info("Getting versions from " + url);
        return doHttpRequest(url);
    }

    public String getFilesUrl(final MavenRepoConfig repoConfig, final MavenPackageConfig packageConfig, final String revision) {
        return concatUrl(repoConfig.getRepoUrlAsString(), packageConfig.getGroupId(), packageConfig.getArtifactId(), revision);
    }

    public String getFilesUrlWithBasicAuth(final MavenRepoConfig repoConfig, final MavenPackageConfig packageConfig, final String revision) {
        return concatUrl(repoConfig.getRepoUrlAsStringWithBasicAuth(), packageConfig.getGroupId(), packageConfig.getArtifactId(), revision);
    }
}
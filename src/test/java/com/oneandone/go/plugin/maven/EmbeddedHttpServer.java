package com.oneandone.go.plugin.maven;

import lombok.NonNull;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** An embedded server that serves resources from a filesystem path. */
public class EmbeddedHttpServer {

    private final Server server;

    /** Can be null, waiters will get notified over the monitor below. */
    private Integer runningPort;

    private final Object monitor;

    private final Runnable serverRunnable;

    private ExecutorService executorService;

    private HandlerList resourceHandler;

    private SecurityHandler securityHandler;

    /** Creates a new instance.
     * */
    public EmbeddedHttpServer() {
        monitor = new Object();
        server = new Server();

        final ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(0);
        server.addConnector(serverConnector);

        serverRunnable = () -> {
            try {
                server.start();
                runningPort = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
                synchronized (monitor) {
                    monitor.notifyAll();
                }
                server.join();
            } catch (final Exception e) {
                throw new IllegalStateException("Could not initialize server", e);
            }
        };
    }

    /**
     * Configures basic authentication.
     * @param userPassword user password pairs that can authenticate.
     * @return this instance.
     */
    public EmbeddedHttpServer withBasicAuth(@NonNull final Map<String,String> userPassword) {
        HashLoginService loginService = new HashLoginService();
        UserStore myUserStore = new UserStore();
        userPassword.forEach((key, value) -> myUserStore.addUser(key, Credential.getCredential(value), new String[]{"user"}));
        loginService.setUserStore(myUserStore);

        server.addBean(loginService);

        ConstraintSecurityHandler security = new ConstraintSecurityHandler();

        securityHandler = security;
        server.setHandler(security);

        Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[]{"user"});

        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec("/*");
        mapping.setConstraint(constraint);

        security.setConstraintMappings(Collections.singletonList(mapping));
        security.setAuthenticator(new BasicAuthenticator());
        security.setLoginService(loginService);

        if (resourceHandler != null) {
            security.setHandler(resourceHandler);
        }
        return this;
    }

    /**
     * Configures a resource path to fetch files from.
     * @param resourcePath the filesystem path to serve resources from.
     * @return this instance.
     */
    public EmbeddedHttpServer withPath(@NonNull final File resourcePath) {
        final ResourceHandler myResourceHandler = new ResourceHandler();

        myResourceHandler.setDirectoriesListed(true);
        myResourceHandler.setResourceBase(resourcePath.getAbsolutePath());

        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{myResourceHandler, new DefaultHandler()});
        if (securityHandler != null) {
            securityHandler.setHandler(handlers);
        } else {
            server.setHandler(handlers);
        }
        this.resourceHandler = handlers;
        return this;
    }

    public int getRunningPort() {
        synchronized(monitor) {
            while (runningPort == null) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    throw new IllegalStateException("Should not get interrupted");
                }
            }
        }
        return runningPort;
    }

    public void start() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(serverRunnable);
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            // ignore
        }
        executorService.shutdown();
    }
}

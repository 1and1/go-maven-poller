package com.oneandone.go.plugin.maven;

import lombok.NonNull;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** An embedded server that serves resources from a filesystem path. */
public class EmbeddedHttpServer {

    private final Server server;

    /** Can be null, waiters will get notified over the monitor below. */
    private Integer runningPort;

    private final Object monitor;

    private final Thread serverThread;

    private ExecutorService executorService;

    /** Creates a new instance.
     * @param resourcePath the filesystem path to serve resources from.
     * */
    public EmbeddedHttpServer(@NonNull final File resourcePath) {
        monitor = new Object();
        server = new Server();

        final ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(0);
        server.addConnector(serverConnector);

        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setResourceBase(resourcePath.getAbsolutePath());

        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resourceHandler, new DefaultHandler()});
        server.setHandler(handlers);

        serverThread = new Thread(() -> {
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
        });
    }

    public int getRunningPort() {
        synchronized(monitor) {
            while (runningPort == null) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return runningPort;
    }

    public void start() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(serverThread);
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

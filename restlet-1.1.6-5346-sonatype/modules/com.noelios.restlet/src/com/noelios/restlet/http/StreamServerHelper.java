/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * HTTP server helper based on NIO blocking sockets.
 * 
 * @author Jerome Louvel
 */
public class StreamServerHelper extends HttpServerHelper {

    /**
     * Class that handles an incoming socket.
     */
    private static class ConnectionHandler implements Runnable {

        /** The target server helper. */
        private final StreamServerHelper helper;

        /** The socket connection to handle. */
        private final Socket socket;

        /**
         * Constructor.
         * 
         * @param helper
         *            The target server helper.
         * @param socket
         *            The socket connection to handle.
         */
        private ConnectionHandler(StreamServerHelper helper, Socket socket) {
            this.helper = helper;
            this.socket = socket;
        }

        /**
         * Handles the given socket connection.
         */
        public void run() {
            try {
                this.helper
                        .handle(new StreamServerCall(this.helper.getHelped(),
                                new BufferedInputStream(this.socket
                                        .getInputStream()),
                                new BufferedOutputStream(this.socket
                                        .getOutputStream()), this.socket));
            } catch (IOException ex) {
                this.helper.getLogger().log(Level.WARNING,
                        "Unexpected error while handling a call", ex);
            }
        }
    }

    /**
     * Listens on the given socket channel for incoming connections and
     * dispatches them to the given handler pool
     */
    private static class Listener implements Runnable {

        /** The target server helper. */
        private final StreamServerHelper helper;

        /** The server socket channel to listen on. */
        private final ServerSocketChannel serverSocket;

        /**
         * The latch to countdown when the socket is ready to accept
         * connections.
         */
        private final CountDownLatch latch;

        /** The handler service. */
        private final ExecutorService handlerService;

        /**
         * Constructor.
         * 
         * @param helper
         *            The target server helper.
         * @param serverSocket
         *            The server socket channel to listen on.
         * @param latch
         *            The latch to countdown when the socket is ready to accept
         *            connections.
         * @param handlerService
         *            The handler service.
         */
        private Listener(StreamServerHelper helper,
                ServerSocketChannel serverSocket, CountDownLatch latch,
                ExecutorService handlerService) {
            this.helper = helper;
            this.serverSocket = serverSocket;
            this.latch = latch;
            this.handlerService = handlerService;
        }

        /**
         * Listens on the given server socket for incoming connections.
         */
        public void run() {
            this.latch.countDown();
            for (;;) {
                try {
                    final SocketChannel client = this.serverSocket.accept();
                    if (!this.handlerService.isShutdown()) {
                        this.handlerService.submit(new ConnectionHandler(
                                this.helper, client.socket()));
                    }
                } catch (ClosedByInterruptException ex) {
                    this.helper.getLogger().log(Level.FINE,
                            "ServerSocket channel was closed by interrupt", ex);
                    break;
                } catch (IOException ex) {
                    this.helper.getLogger().log(Level.WARNING,
                            "Unexpected error while accepting new connection",
                            ex);
                }
            }
        }
    }

    /**
     * Thread factory that logs uncaught exceptions thrown by the created
     * threads.
     */
    private static class LoggingThreadFactory implements ThreadFactory {

        private class LoggingExceptionHandler implements
                Thread.UncaughtExceptionHandler {

            public void uncaughtException(Thread t, Throwable ex) {
                LoggingThreadFactory.this.logger.log(Level.SEVERE, "Thread: "
                        + t.getName() + " terminated with exception: "
                        + ex.getMessage(), ex);
            }
        }

        private final Logger logger;

        public LoggingThreadFactory(Logger logger) {
            this.logger = logger;
        }

        public Thread newThread(Runnable r) {
            final Thread result = new Thread(r);
            result.setUncaughtExceptionHandler(new LoggingExceptionHandler());
            return result;
        }
    }

    /** The connection handler service. */
    private volatile ExecutorService handlerService;

    /** The socket listener service. */
    private volatile ExecutorService listenerService;

    /** The server socket channel. */
    private volatile ServerSocketChannel serverSocketChannel;

    /** The synchronization aid between listener and handler service. */
    private volatile CountDownLatch latch;

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public StreamServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTP);
    }

    /**
     * Create a server socket channel and bind it to the given address
     * 
     * @return Bound server socket channel.
     * @throws IOException
     */
    protected ServerSocketChannel createServerSocket() throws IOException {
        final ServerSocketChannel server = ServerSocketChannel.open();
        server.socket().bind(createSocketAddress());
        return server;
    }

    /**
     * Creates a socket address to listen on.
     * 
     * @return The created socket address.
     * @throws IOException
     */
    protected SocketAddress createSocketAddress() throws IOException {
        if (getHelped().getAddress() == null) {
            return new InetSocketAddress(getHelped().getPort());
        } else {
            return new InetSocketAddress(getHelped().getAddress(), getHelped()
                    .getPort());
        }
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        getLogger().info("Starting the internal HTTP server");

        final ThreadFactory factory = new LoggingThreadFactory(getLogger());

        // Configure the thread services
        this.handlerService = Executors.newFixedThreadPool(10, factory);
        this.listenerService = Executors.newSingleThreadExecutor(factory);

        // Create the server socket
        this.serverSocketChannel = createServerSocket();

        // Sets the ephemeral port is necessary
        setEphemeralPort(this.serverSocketChannel.socket());

        // Start the socket listener service
        this.latch = new CountDownLatch(1);
        this.listenerService.submit(new Listener(this,
                this.serverSocketChannel, this.latch, this.handlerService));

        // Wait for the listener to start up and count down the latch
        // This blocks until the server is ready to receive connections
        try {
            this.latch.await();
        } catch (InterruptedException ex) {
            getLogger()
                    .log(
                            Level.WARNING,
                            "Interrupted while waiting for starting latch. Stopping...",
                            ex);
            stop();
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        getLogger().info("Stopping the internal HTTP server");

        if (this.handlerService != null) {
            // Gracefully shutdown the handlers, they should complete
            // in a timely fashion
            this.handlerService.shutdown();
            try {
                this.handlerService.awaitTermination(30,
                        TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
				getLogger().log(Level.FINE,
						"Interruption while shutting down internal server", ex);
			}
		}

        if (this.listenerService != null) {
            // This must be forcefully interrupted because the thread
            // is most likely blocked on channel.accept()
            this.listenerService.shutdownNow();

            try {
                this.listenerService.awaitTermination(30,
                        TimeUnit.SECONDS);
            } catch (Exception ex) {
				getLogger().log(Level.FINE,
						"Interruption while shutting down internal server", ex);
			}
		}

        // Close the server socket
        if (this.serverSocketChannel != null) {
            this.serverSocketChannel.close();
        }
    }
}

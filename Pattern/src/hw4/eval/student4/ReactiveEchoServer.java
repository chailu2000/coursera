package hw4.eval.student4;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

/**
 * The class implements simple echo server based on Netty framework.
 * Implementation demonstrates usage of Wrapper Facade, Reactor and Acceptor
 * patterns (as part of Acceptor-Connector pattern).
 */
public class ReactiveEchoServer {
    // Default server port used when no program arguments specified
    private static final int DEFAULT_SERVER_PORT = 8082;

    // Holds server port number
    private static int SERVER_PORT;

    // Holds reference to helper bootstrap class
    private ServerBootstrap bootstrap;

    /**
     * Creates and configures a new ReactiveEchoServer.
     */
    public ReactiveEchoServer(int port) {
        // Represents main acceptor threads. One thread will be initiated per
        // each server socket, so one client channel acceptor per server port.
        //
        // This thread pool implements the "Sync Task" part of the
        // Half-Sync/Half-Async pattern. The thread will operate with selector
        // which selects events, groups them by type (connect events/write
        // events) in Netty and puts to the priority queue for further
        // processing by threads from the handler thread pool.
        ExecutorService acceptorThreads = Executors.newCachedThreadPool();

        // Represents worker threads that will operate pipeline handlers.
        // Relation between worker threads and client channels can be one to
        // many (so for example 100 worker threads can process 10k client
        // channels, this is part of Netty design).
        //
        // This thread pool represents "Async Task" part of the
        // Half-Sync/Half-Async pattern. Threads in this thread pool will be
        // responsible for asyncronous processing of chunks coming from the
        // socket as result of select operation.
        //
        // For this example, number of threads is set to 10.
        ExecutorService handlerWorkerThreads = Executors.newFixedThreadPool(10);

        // Partially represents both Acceptor and Reactor pattern
        // implementations.
        //
        // NOTE: Due to specific framework design, it is not possible to 100%
        // clearly map all classes to the required pattern components. This is
        // due Acceptor and Reactor already implemented in Netty.
        //
        // ACCEPTOR
        // --------
        // Each new incoming connection is:
        // 1. processed by acceptor thread which accept client connection
        // 2. new pipeline (and handler) instances are created and handlers
        // registered in Reactor
        //
        // REACTOR
        // -------
        // * Implements main selection loop
        // * Incoming events are processed by corresponding handlers previously
        // registered by Acceptor
        //
        // Acceptor and Reactor are activated after bind operation is invoked by
        // server bootstrap.
        ChannelFactory channelFactory = new NioServerSocketChannelFactory(
                acceptorThreads, handlerWorkerThreads);

        // Represents pipeline factory, creates new pipeline per each client
        // connection.
        // In our case handler is shared, so maintains no state.
        ReactiveEchoServerPipelineFactory pipelineFactory = new ReactiveEchoServerPipelineFactory();

        // Configure helper bootstrap class with channel and pipeline factories
        bootstrap = new ServerBootstrap(channelFactory);
        bootstrap.setPipelineFactory(pipelineFactory);

        // This is just for demo purpose, some other options can be configured
        bootstrap.setOption("keepAlive", true);

        // Set the server port number
        SERVER_PORT = port;
    }

    /**
     * Starts the server. Performs binding to server socket which in turn
     * activates Acceptor and Reactor threads.
     */
    public void start() {
        System.out
                .println("ReactiveEchoServer - Starting Reactive Echo Server, powered by Netty");

        // Part of Acceptor pattern. Binds to defined server port and initiates
        // acceptor, main reactor selection, and worker threads
        Channel serverChannel = bootstrap.bind(new InetSocketAddress(
                SERVER_PORT));

        // Check whether binding was successfull and do some logging
        if (serverChannel.isBound()) {
            System.out.println("ReactiveEchoServer - Bound to port "
                    + SERVER_PORT + ", start listening for incoming events");
        } else {
            System.out.println("ReactiveEchoServer - Binding to port "
                    + SERVER_PORT + " FAILED.");
        }
    }

    /**
     * Stops the server by releasing external resources.
     */
    public void stop() {
        this.bootstrap.releaseExternalResources();
        System.out.println("ReactiveEchoServer - Server stopped.");
    }

    private static int configureServerPort(String args[]) {
        return args.length == 1 ? Integer.parseInt(args[0])
                : DEFAULT_SERVER_PORT;
    }

    public static void main(String args[]) {
        // Init and start reactive echo server
        final ReactiveEchoServer server = new ReactiveEchoServer(
                configureServerPort(args));
        server.start();

        // Try graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.stop();
            }
        });
    }

    /**
     * Class implements echo handler.
     */
    public static class ReactiveEchoServerHandler extends
            SimpleChannelUpstreamHandler {
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
                throws Exception {
            System.out
                    .println("ReactiveEchoServer - Received message from client: ["
                            + e.getMessage() + "]; echo it back.");

            // The Channel represents implementation of the Wrapper Facade
            // pattern.
            Channel channel = e.getChannel();
            channel.write(e.getMessage());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            System.out.println("ReactiveServer: Exception caught - "
                    + e.getCause());
        }
    }

    /**
     * Class implements factory which configures a new pipeline instance per
     * each new client channel.
     */
    public static class ReactiveEchoServerPipelineFactory implements
            ChannelPipelineFactory {
        @Override
        public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline pipeline = Channels.pipeline();

            // Uncommenting of the two below handlers will let you to read
            // client messages in logs as strings
            // pipeline.addLast("stringDecoder", new StringDecoder());
            // pipeline.addLast("stringEncoder", new StringEncoder());
            pipeline.addLast("handler", new ReactiveEchoServerHandler());

            System.out
                    .println("ReactiveEchoServer - Accepted new client connection, create pipeline "
                            + pipeline);
            return pipeline;
        }
    }
}
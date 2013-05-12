package hw4.eval.student2;

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
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * Echo server using <a href="http://netty.io/">netty.io</a>.<br />
 * <br />
 * <b>Most of the code is taken from the netty documentation</b> and is
 * documented by me to describe which code part implements which pattern.<br />
 * <br />
 * There is not much room for an own implementation, because the framework
 * implements most of the patterns (which is the justification for a framework!)
 * and only a few lines of own code is necessary to use them. <br />
 * <br />
 * Coded with netty 3.6.5.Final, run with the program: <br />
 * 
 * <pre>
 * java -cp .:netty.jar Program <port_number>
 * </pre>
 * 
 * Port number is optional and defaults to 8080 if missing.<br />
 * Stop the server with CTRL+C.<br />
 * Connect to server with:
 * 
 * <pre>
 * telnet localhost 8080
 * </pre>
 */
public class Program {

    /**
     * Main program.
     * @param args command line arguments
     */
    public static void main(String[] args) {

        final int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        // create a factory from an implementation of ServerSocketChannelFactory
        // with the thread pools to handle connection and work

        // HALF SYNC / HALF ASYNC: only one halfAsyncThread -> internal queue ->
        // multiple halfSyncThread(s)
        final ExecutorService halfAsyncThread = Executors.newFixedThreadPool(1);
        final ExecutorService halfSyncThreads = Executors.newCachedThreadPool();

        // REACTOR: ChannelFactory: if a connection is accepted, the
        // ChannelPipeline is connected to the channel and handles the request
        final ChannelFactory factory = new NioServerSocketChannelFactory(halfAsyncThread, halfSyncThreads);

        // create a server with the factory
        //
        // WRAPPER FACADE: ServerBootstrap abstracts the lower level network
        // interfaces to bind to ports, handle socket data etc.
        final ServerBootstrap bootstrap = new ServerBootstrap(factory);

        // set up the pipeline factory and add it to the server
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            /**
             * {@inheritDoc}
             */
            public ChannelPipeline getPipeline() {
                // HALF ASYNC of HALF SYNC / HALF ASYNC
                System.out.println("getPipeline of thread " + Thread.currentThread().getName());
                // attach the EchoServerHandler to the pipeline
                return Channels.pipeline(new EchoServerHandler());
            }
        });

        // set some options
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        // bind the server to the port. Blocks until Ctrl+C
        bootstrap.bind(new InetSocketAddress(port));
    }

    /**
     * The handler that echoes the input. <br />
     * ACCEPTOR: the SimpleChannelUpstreamHandler internally takes the content
     * and calls the method
     * {@link #messageReceived(ChannelHandlerContext, MessageEvent)}. <br />
     * HALF SYNC of HALF SYNC / HALF ASYNC: thread that handles a connection
     */
    private static class EchoServerHandler extends SimpleChannelUpstreamHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

            System.out.println("messageReceived of thread " + Thread.currentThread().getName());

            final Channel ch = e.getChannel();
            final Object message = e.getMessage();
            ch.write(message);
        }
    }
}


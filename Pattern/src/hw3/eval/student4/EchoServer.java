package hw3.eval.student4;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;


public class EchoServer {

    private final int   port;

    private static void usage() {
        System.out.println("Usage: java EchoServer + port_number\n\n    port_number - " +
                "a port to listen in range 100-65535");
        System.exit(-1);

    }

    public static void main(String[] args) {
        // check command line arguments
        if (args.length != 1) {
            usage();
        } else {
            int portNumber = 0;
            try {
                portNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                usage();
            }

            if (portNumber < 100 || portNumber > 65535)
                usage();

            new EchoServer(portNumber).run();
        }
    }
    
    public EchoServer(int port) {
        this.port = port;
    }

    public void run() {
        // Configure the server. Create a thread pool for this server
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Create a ChannelPipelineFactory which sets up a pipeline for processing the inbound messages;
        //      the EchoServerHandler should be the only handler in the pipeline.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new EchoServerHandler());
            }
        });

        // Configure the ServerSocketChannelFactory that inherits from ServerChannelFactory, 
        // and binds the ServerBootstrap class to an appropriate InetSocketAddress.
        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(port));
    }

    

    public class EchoServerHandler extends SimpleChannelUpstreamHandler {
        //use by clients to disconnect
        private static final byte CTRL_D    = 0x04;
        private static final String BYE_MESSAGE  = "Bye!\n";
        
        // accumulate chars until EOL
        private StringBuffer accumulator = new StringBuffer();
        
        private final AtomicLong    transferredBytes    = new AtomicLong();

        public long getTransferredBytes() {
            return transferredBytes.get();
        }

        @Override
        public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            String clientAddr = e.getChannel().getRemoteAddress().toString().substring(1);
            String message = "Hello, " + clientAddr + "!\n\n"; 
            System.out.println("Client is connected from: " + clientAddr);
            sendString(e, message);
            super.channelOpen(ctx, e);
        }
        
        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            String clientAddr = e.getChannel().getRemoteAddress().toString().substring(1);
            System.out.println("Client is disconnected from: " + clientAddr);
            super.channelClosed(ctx, e);
        }
        
        
        
        private void sendString(ChannelEvent e, String message) {
            e.getChannel().write(ChannelBuffers.wrappedBuffer(message.getBytes()));
        }
        
        @Override
        public void messageReceived(
                ChannelHandlerContext ctx, MessageEvent e) {
            // Send back the received message to the remote peer.
            transferredBytes.addAndGet(((ChannelBuffer) e.getMessage()).readableBytes());
            
            ChannelBuffer buf = (ChannelBuffer) e.getMessage();
            while (buf.readable()) {
                // if client wants to terminate
                if(buf.getByte(0) == CTRL_D) {
                    sendString(e, BYE_MESSAGE);
                    e.getChannel().close();
                    return;
                }
                
                char nextChar = (char) buf.readByte();
                if (nextChar == '\n' || nextChar == '\r') {
                    accumulator.append("\n");
                    sendString(e, accumulator.toString());
                    
                    if (accumulator.length() > 0)
                        accumulator.delete(0, accumulator.length());
                } else {
                    accumulator.append(nextChar);
                }
            }
              
        }

        @Override
        public void exceptionCaught(
                ChannelHandlerContext ctx, ExceptionEvent e) {
            // Close the connection when an exception is raised.
            System.out.println("Unexpected exception from downstream." + e.getCause());
            e.getChannel().close();
        }
    }

}


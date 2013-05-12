package hw3.eval.student2;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;


/**
 * Purpose:        Programming Assignment Number 3
 * Problem:     Simple EchoServer Using Java Netty Framework
 * References:    1. Java Netty EchoServer Example (http://docs.jboss.org/netty/3.2/guide/eclipse/start.html#d0e375)
 *                 2. Java Netty Use of Decoders and Encoders (https://class.coursera.org/posa-001/forum/thread?thread_id=1066&post_id=3693#post-3693)
 *
 * How to Run:    java -cp .;netty.jar Program port_number
 *                 If Port_number is blank then a default port number of 2323 is used
 */
public class Program {

    private static final int DEFAULT_PORT = 2323;        //Default port number
    private static final int FRAME_SIZE = 8192;            //Frame size for DelimiterBasedFrameDecoder
    
    /**
     * This class acts as a Daemon class and will start the Reactor event loop i.e. it will bind the ServerSocketChannel to the specified port number
     */
    public static class EchoServerDaemon {        

        private final int port;        
        
        //The ServerBootstrap class is analogous to ACE_Reactor class in the ACE reactor framework.
        //See more details in the class descripiton below
        ServerBootstrap reactor;
        
        
        public EchoServerDaemon(int port, ServerBootstrap reactor) {
            this.port = port;
            this.reactor = reactor;
        }
        
        //This method will start the main EventLoop for the EchoServer.
        public void start() {
            reactor.bind(new InetSocketAddress(this.port));
            
            System.out.println("Echo Server started listening on port " + this.port);
        }
        
    }
    
    
    /**
     * EchoServerReactor class extends from the ServerBootstrap class.
     * The ServerBootstrap class is analogous to ACE_Reactor class in the ACE reactor framework.
     *
     * The ServerBootstrap will be initialized with ServerSocketChannelFactory which creates a Server Socket Channel
     * and ServerChannelPipelineFactory which creates a Channel Pipeline. Channel pipeline contains a list of Event Handlers
     *
     * ServerSocketChannel is a concrete implementation of Channel interface which which acts as Wrapper Facade over the underlying Java Sockets and I/O API.
     * In the standard Reactor Pattern notation, ServerSocketChannel class will correspond to the Handle entity.  
     *
     * ChannelPipeline is basically a list of Event Handlers that will be executed in sequence on the triggering of Events on a Channel.
     *
     */
    public static class EchoServerReactor extends ServerBootstrap {        


        //This constructor will initialize the ServerBootStrap object with ServerSocketChannelFactory and ServerChannelPipelineFactory.
        //EchoServerEventHandler is the handler with which the ChannelPipelineFactory will be initialized.
        public EchoServerReactor(EchoServerEventHandler handler) {
            
            //Initializes the ServerSocketChannelFacotry
            super(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),Executors.newCachedThreadPool()));
            
            //Sets the ServerChannelPipelineFactory
            this.setPipelineFactory(new EchoServerChannelPipelineFactory(handler));            
            
        }        
        
        /**
         * Concrete implementation of ChannelPipelineFactory interface.
         *
         */
        private static class EchoServerChannelPipelineFactory implements ChannelPipelineFactory {
            
            //The pipeline will alwasy be created with this EventHandler object.
            private EchoServerEventHandler eventToDispatch;
            
            //Additional Handlers for the Encoding/Decoding of raw bytes to String
            private static final StringDecoder DECODER = new StringDecoder();
            private static final StringEncoder ENCODER = new StringEncoder();
            
            //Additional Handler that will handle the line break character
            private DelimiterBasedFrameDecoder frameDecoder ;
            
            public EchoServerChannelPipelineFactory(EchoServerEventHandler eventHandler) {
                this.eventToDispatch = eventHandler;
                frameDecoder = new DelimiterBasedFrameDecoder(FRAME_SIZE , Delimiters.lineDelimiter());
            }
            
            @Override
            //Return a new ChannelPipeline always containing the single EventHandler 'eventToDispatch'
            //First add the DelimiterBasedFrameDecoder, then StringDecoder,then String Encoder and at last EchoServerEventHandler
            public ChannelPipeline getPipeline() throws Exception {                
                return Channels.pipeline(frameDecoder,DECODER,ENCODER,this.eventToDispatch);
            }
            
        }
        
    }
    
    /**
     * This class corresponds to the ACE_Event_Handler and ACE_SVC_Handler classes in ACE Reactor Framework
     * Basically, this class override the hook methods for handling various kinds of events like Connection Acceptance, handling of peer messages  etc.
     *
     * This class plays the role Acceptor as well as Service Handlers in the ACE Reactor-Acceptor-Connector framework
     *
     * This class also fulfills the assignment requirement of creating EchoServerHandler that inherits from SimpleChannelUpstreamHandler
     */
    public static class EchoServerEventHandler extends SimpleChannelUpstreamHandler {

        private static final Logger logger = Logger.getLogger(EchoServerEventHandler.class.getName());
    
        @Override
        //This is the hook method which will be called by our reactor i.e. ServerBootstrap class on receiving a message from the client (peer)
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {            
            // Send back the received message to the remote peer.
            // Note that DelimiterBasedFrameDecoder handler in the ChannelPipeline factory ensures that a single character is never written out to the peer
            e.getChannel().write(e.getMessage());
            
        }

        @Override
        //Hook method that will be called for handling exceptions
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            // Close the connection when an exception is raised.
            logger.log(Level.WARNING, "Not able to process the client request.",e.getCause());
            e.getChannel().close();
        }

        @Override
        public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)  {            
            logger.log(Level.INFO, "Client: "  + e.getChannel().getRemoteAddress() + " Connected.");
        }

        @Override
        public void channelDisconnected(ChannelHandlerContext ctx,    ChannelStateEvent e)  {
            logger.log(Level.INFO, "Client: "  + e.getChannel().getRemoteAddress() + " Disconnected.");
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = DEFAULT_PORT;
        }
        
        //Create new Echo Server EventHandler
        EchoServerEventHandler eventHandler = new EchoServerEventHandler();
        
        //Create the Reactor object
        EchoServerReactor reactor = new EchoServerReactor(eventHandler);
        
        //Create the Server Daemon object
        EchoServerDaemon daemon = new EchoServerDaemon(port,reactor);
        
        //Start the Daemon
        daemon.start();

    }

}
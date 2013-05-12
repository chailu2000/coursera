package hw4.eval.student1;

/**
 * Filename: Echo_ReactiveServer
 * Purpose: Programming Assignment-3
 * Description : This program implements a Echo ReactiveServer using java netty SDK and shows example 
 * of Wrapper Facade, Reactor/Acceptor-Connector pattern.
 * 
 * Half-Sync/Half-Async :
 *   NioServerSocketChannelFactory : Refer to http://netty.io/3.6/api URL. (Boss Thread correspond to server thread that accepts 
 *   the incoming client connection where as Worker Thread is used to handle the request in an asynchronous way.)
 *   
 * Wrapper Facade :
 *   ServerChannelFactory : Factory pattern to create a Channel which abstracts a universal asynchronous IO (commonality). It shields
 *   all underlying OS/Platform specific (java socket level) details. Specific implementation (variability) can be Java NIO (TCP/IP) 
 *   or OIO (TCP or UDP) based transport. 
 *   
 *   ChannelBuffers:
 *     Provides abstraction for all buffer/IO-stream managements. It can be over NIO Buffers or Old style java buffers.
 *   
 * Reactor Thread and Acceptor/Connector Thread.
 *   NioServerChannelFactory does not create IO Thread by itself but it takes two thread pools as parameters for reactor and 
 *   acceptor execution/thread resources. One can pass in thread pool with a # of threads considering # of cores available 
 *   on the server. Reactor threads are separate from threads that actually handle client requests (in this case echo the message).
 *   
 * Message Handlers :
 *   ServerBootstrap : This class separates the connection management (connecting/disconnecting and connection state) from message 
 *   handling. It takes a Pipeline Factory which specifies one or more message handlers. In Echo Server case, there is only one
 *   message handler. Message handler receives a message and handles (echos) it. Message handling concerns are separated from
 *   connection management concerns.
 *   
 * Echo Client can be invoked simply by executing 
 *    telnet localhost 8080
 *   
 */
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * Main class for Echo Server.
 */
public class Echo_ReactiveServer {

	private int _port;

	/**
	 * Ctor.
	 * @param port to listen on.
	 */
	Echo_ReactiveServer(int port) {
		_port = port;
	}
	
	/**
	 * Creates a Channel Factory, associates with PipelineFactory that provides message handler and binds to socket and port
	 * (connection resources to use).
	 * 
	 * @throws Exception
	 */
	public void runServer() throws Exception {
		
		// Takes a thread pools for Reactor (select) and Message handler processing. 
		ServerSocketChannelFactory sscfactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				                                                                  Executors.newCachedThreadPool());
		ServerBootstrap bootstrap = new ServerBootstrap(sscfactory);
		
		// Message Handler via Pipeline factory.
		bootstrap.setPipelineFactory(new EchoChannelPipelineFactory());
		
		// bind to the port to listen on
		bootstrap.bind(new InetSocketAddress(_port));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// default value can be overriden by program argument
		int server_port = 8080;   
		
		if ( args.length > 0 ) 
		{
			try {
				// In case you dont want to listen on default port.
				int p = Integer.parseInt(args[0]);	
				server_port = p;
			} catch (NumberFormatException nfe) {
				System.out.println("Invalid port " + args[0] + " value specified. Will use default " + server_port);
			}
		}
		
		System.out.println("Echo_ReactiveServer started...");
		
		Echo_ReactiveServer echoServer = new Echo_ReactiveServer(server_port);
		try {
			echoServer.runServer();
		} catch (Exception e) {
			System.out.println("Exception encountered..");
			e.printStackTrace();
		}
		
	}
}

/**
 * Message Handler class.
 *
 */
class EchoServerHandler extends SimpleChannelUpstreamHandler {

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		super.channelOpen(ctx, e);
		InetSocketAddress remoteAddress = (InetSocketAddress) e.getChannel().getRemoteAddress();
		StringBuilder preambleStr = new StringBuilder( "[channelOpen : remote-addr " + 
		                                                remoteAddress.getHostName() + 
		                                                " port: " + remoteAddress.getPort() + "] " );
		preambleStr.append(" channelState = " + e.getState().toString());
		preambleStr.append(" channelFuture = " + e.getFuture().toString());
		System.out.println(preambleStr.toString());
	}

	// Just to track when client connects.
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		super.channelConnected(ctx, e);
		InetSocketAddress remoteAddress = (InetSocketAddress) e.getChannel().getRemoteAddress();
		String preamble = "[Msg From " + remoteAddress.getHostName() + " port: " + remoteAddress.getPort() + "] ";
		System.out.println(preamble + "Channel connected.");
	}

	// Just to track when client dis-connects
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		super.channelDisconnected(ctx, e);
		InetSocketAddress remoteAddress = (InetSocketAddress) e.getChannel().getRemoteAddress();
		String preamble = "[Msg From " + remoteAddress.getHostName() + " port: " + remoteAddress.getPort() + "] ";
		System.out.println(preamble + "Channel disconnected.");
	}

	// default ctor
	public EchoServerHandler() {}
	
	// exception handler
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent event)
			throws Exception {
		System.out.println("Exception occured, closing the channel.");
		event.getChannel().close();
	}

	// message handler
	// This is where the incoming message is reflected-back or echoed back to client.
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		
		InetSocketAddress remoteAddress = (InetSocketAddress) e.getRemoteAddress();
		String preamble = "[Msg From " + remoteAddress.getHostName() + " port: " + remoteAddress.getPort() + "] ";
		Channel outputChannel = e.getChannel();
		ChannelBuffer chBuf = (ChannelBuffer) e.getMessage();
		if ( chBuf.readableBytes() > 0 ) {
			byte msgRead[] = new byte[chBuf.readableBytes()];
			chBuf.markReaderIndex();
			// Read for displaying it one server side (just for debugging). 
			// Echo server does not need to do it for just reflecting back.
			chBuf.readBytes(msgRead);   
			// Reset the reader cursor so no copy need be made
			chBuf.resetReaderIndex();
			System.out.println(preamble + new String(msgRead));
			// echo back
			outputChannel.write(chBuf);
		}
	}
}

/*
 * Echo Pipeline Factory. Only handler needed is Echo Handler.
 */
class EchoChannelPipelineFactory implements ChannelPipelineFactory {

    private final static EchoServerHandler _echoHandler = new EchoServerHandler();	
	public EchoChannelPipelineFactory() {
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
    	return Channels.pipeline(_echoHandler); 
	}
}






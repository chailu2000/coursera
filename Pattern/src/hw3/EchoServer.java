/**
 * Pattern oriented architecture HW 3
 * Java version 1.7.0_15
 * Netty library version 3.6.5.Final
 */

package hw3;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * Main class - wraps around the EchoServerImpl class of detailed implementation
 * Default port 8080 or the first argument
 * @author ubuntu
 * 
 */
public class EchoServer {
	private static final int DEFAULT_LISTENING_PORT = 8080;

	public static void main(String[] args) {
		int portNumber = DEFAULT_LISTENING_PORT;
		if (args.length == 1) {
			try {
				portNumber = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("Please enter a valid port number.");
				System.exit(1);
			}
		}
		EchoServerImpl server = new EchoServerImpl();
		server.serve(portNumber);
	}
}

/**
 * Echo Server implementation using Netty library
 * The ServerBootStrap class plays the Wrapper Facade role by hiding the low
 * 		level implementation of socket binding, etc.
 * The ServerBootStrap class also plays the Reactor role by dispatching events
 * 		through Channel and Channel Pipelines
 * The NioServerSocketChannleFactory class plays the Connector role in the
 * 		Acceptor-Connector pattern
 * @author ubuntu
 * 
 */
class EchoServerImpl {
	private static final ChannelGroup allChannels = new DefaultChannelGroup(
			"echo-server");

	public void serve(int portNumber) {

		final ChannelFactory sscFactory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new EchoServerHandler());
			}

		});
		bootstrap.setFactory(sscFactory);

		bootstrap.setOption("localAddress", new InetSocketAddress(portNumber));
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		Channel channel = bootstrap.bind();
		allChannels.add(channel);

		// clean up resources when shutdown signal received
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				ChannelGroupFuture future = allChannels.close();
				future.awaitUninterruptibly();
				sscFactory.releaseExternalResources();
			}
		});
	}
}

/**
 * Event handler to echo back message received 
 * Plays the acceptor and the service handler roles in the Acceptor-Connector pattern
 * 
 * @author ubuntu
 * 
 */
class EchoServerHandler extends SimpleChannelUpstreamHandler {
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		StringBuilder echoMsg = new StringBuilder();

		ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
		while (buffer.readable()) {
			char c = (char) buffer.readByte();

			echoMsg.append(c);
			if (c == Character.LINE_SEPARATOR) {
				System.out.println(echoMsg);
				e.getChannel().write(
						ChannelBuffers.wrappedBuffer(echoMsg.toString()
								.getBytes()));
				// an extra line break for the client
				e.getChannel().write(
						ChannelBuffers.wrappedBuffer(System.getProperty(
								"line.separator").getBytes()));
			}

		}
	}

}

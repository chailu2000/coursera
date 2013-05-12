package hw3.eval.student1;

import static org.jboss.netty.channel.Channels.pipeline;
import static org.jboss.netty.channel.Channels.pipelineFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class EchoClient {

	private final String host;
	private final int port;

	private StringBuilder history = new StringBuilder();

	public EchoClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void run() throws IOException {
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(pipelineFactory(pipeline(new EchoClientHandler())));
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

		// Wait until the connection attempt succeeds or fails.
		Channel channel = future.awaitUninterruptibly().getChannel();
		if (!future.isSuccess()) {
			future.getCause().printStackTrace();
			bootstrap.releaseExternalResources();
			return;
		}

		// Read commands from the stdin.
		ChannelFuture lastWriteFuture = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			if (!in.ready()) {
				continue;
			}
			char[] charBuffer = new char[1];
			in.read(charBuffer);
			String messageString = Character.valueOf(charBuffer[0]).toString();
			history.append(messageString);
			byte[] message = messageString.getBytes();

			// Sends the received line to the server.
			ChannelBuffer messageBuffer = ChannelBuffers.buffer(message.length);
			messageBuffer.writeBytes(message);
			lastWriteFuture = channel.write(messageBuffer);

			// If user typed the 'bye' command, wait until the server closes
			// the connection.
			if (history.toString().contains("bye")) {
				channel.getCloseFuture().awaitUninterruptibly();
				break;
			}
		}

		// Wait until all messages are flushed before closing the channel.
		if (lastWriteFuture != null) {
			lastWriteFuture.awaitUninterruptibly();
		}

		// Close the connection. Make sure the close operation ends because
		// all I/O operations are asynchronous in Netty.
		channel.close().awaitUninterruptibly();

		// Shut down all thread pools to exit.
		bootstrap.releaseExternalResources();
	}

	public static class EchoClientHandler extends SimpleChannelUpstreamHandler {
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
			// Print out the line received from the server.
			ChannelBuffer message = (ChannelBuffer) e.getMessage();
			System.out.println(message.toString(Charset.forName("utf-8")));
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			e.getChannel().close();
		}
	}

	public static void main(String[] args) throws Exception {
		new EchoClient("localhost", 8080).run();
	}
}


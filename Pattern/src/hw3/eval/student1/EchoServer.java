package hw3.eval.student1;

import static org.jboss.netty.channel.Channels.pipeline;
import static org.jboss.netty.channel.Channels.pipelineFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelLocal;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class EchoServer {

	private static final Logger logger = Logger.getLogger(EchoServer.class.getName());

	private ServerBootstrap bootstrap;

	public static void main(String[] args) {
		EchoServer echoServer = new EchoServer();
		echoServer.start(8080);
	}

	public EchoServer() {
		ServerSocketChannelFactory channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		bootstrap = new ServerBootstrap(channelFactory);
		EventDispatcher eventDispatcher = new EventDispatcher();
		eventDispatcher.registerHandler(new ChannelBufferHandler());
		bootstrap.setPipelineFactory(pipelineFactory(pipeline(new ConectionAcceptor(eventDispatcher))));
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
	}

	public void start(int port) {
		bootstrap.bind(new InetSocketAddress(8080));
	}

	public static class ConectionAcceptor extends SimpleChannelUpstreamHandler {

		private EventDispatcher eventDispatcher;

		public ConectionAcceptor(EventDispatcher eventDispatcher) {
			this.eventDispatcher = eventDispatcher;
		}

		@Override
		public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {

			// always accept new connections
			super.channelConnected(ctx, e);
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

			// dispatching event
			eventDispatcher.handle(e);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			logger.log(Level.WARNING, "Unexpected exception from downstream.", e.getCause());
			e.getChannel().close();
		}

	}

	public static class EventDispatcher implements EventHandler {

		private Set<EventHandler> handlers = new HashSet<EventHandler>();

		public void registerHandler(EventHandler handler) {
			handlers.add(handler);
		}

		public void unregisterHandler(EventHandler handler) {
			handlers.remove(handler);
		}

		@Override
		public void handle(MessageEvent e) {
			for (EventHandler handler : handlers) {
				if (handler.accepts(e)) {
					handler.handle(e);
				}
			}
		}

		@Override
		public boolean accepts(MessageEvent e) {
			return true;
		}

	}

	public static interface EventHandler {

		void handle(MessageEvent e);

		boolean accepts(MessageEvent e);

	}

	public static class ChannelBufferHandler implements EventHandler {

		@Override
		public void handle(MessageEvent e) {

			// resolving proper protocol
			EchoProtocol echoProtocol = new EchoProtocol(e.getChannel());
			echoProtocol.handleMessage((ChannelBuffer) e.getMessage());
		}

		@Override
		public boolean accepts(MessageEvent e) {
			return e.getMessage() instanceof ChannelBuffer;
		}

	}

	public static class EchoProtocol {

		private static ChannelLocal<StringBuilder> channelLocal = new ChannelLocal<StringBuilder>(true);

		private Channel channel;

		public EchoProtocol(Channel channel) {
			this.channel = channel;
		}

		public void handleMessage(ChannelBuffer message) {

			// accumulating messages
			ChannelWrapperFacade channel = new ChannelWrapperFacade(this.channel, message);
			channel.accumulateMessage();

			// echo accumulated messages if new line symbol detected
			String currentMessage = channel.readMessage();
			if (currentMessage.contains("\r") || currentMessage.contains("\n")) {
				String finalMessage = channel.getAccumulatedMessage();
				channel.writeMessage(finalMessage);

				// say good bye if close command detected
				if (finalMessage.toLowerCase().contains("bye")) {
					ChannelFuture future = channel.writeMessage("Have a nice day");
					future.addListener(ChannelFutureListener.CLOSE);
				}
			}
		}
	}

	public static class ChannelWrapperFacade {

		private Channel channel;

		private MessageWrapperFacade message;

		public ChannelWrapperFacade(Channel channel, ChannelBuffer message) {
			this.channel = channel;
			this.message = new MessageWrapperFacade(message);
		}

		public String readMessage() {
			return message.asString();
		}

		public ChannelFuture writeMessage(String message) {
			return channel.write(new MessageWrapperFacade(message).asChannelBuffer());
		}

		public void accumulateMessage() {
			getChannelLocalValue().append(readMessage());
		}

		public String getAccumulatedMessage() {
			String message = getChannelLocalValue().toString();
			EchoProtocol.channelLocal.set(channel, new StringBuilder());
			return message;
		}

		private StringBuilder getChannelLocalValue() {
			EchoProtocol.channelLocal.setIfAbsent(channel, new StringBuilder());
			return EchoProtocol.channelLocal.get(channel);
		}

		private static class MessageWrapperFacade {

			private ChannelBuffer message;

			public MessageWrapperFacade(ChannelBuffer message) {
				this.message = message;
			}

			public MessageWrapperFacade(String message) {
				this.message = ChannelBuffers.dynamicBuffer();
				this.message.writeBytes(message.getBytes());
			}

			public String asString() {
				return message.toString(Charset.forName("utf-8"));
			}

			public ChannelBuffer asChannelBuffer() {
				return message;
			}

		}

	}

}

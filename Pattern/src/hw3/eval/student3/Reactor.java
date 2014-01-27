package hw3.eval.student3;

import java.net.InetSocketAddress;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

public class Reactor {
	
	public static void main(String[] args) {
		ChannelFactory channelFactory = new NioServerSocketChannelFactory();
		ServerBootstrap server = new ServerBootstrap(channelFactory);

		ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
			
			final EchoServerHandler handler = new EchoServerHandler();
			final StringDecoder stringDecoder = new StringDecoder();
			final StringEncoder stringEncoder = new StringEncoder();
			
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("framer", new DelimiterBasedFrameDecoder(1024, false, Delimiters.lineDelimiter()));
				pipeline.addLast("decoder", stringDecoder);
				pipeline.addLast("handler", handler);
				pipeline.addLast("encoder", stringEncoder);
				return pipeline;
			}
		};
		
		server.setPipelineFactory(pipelineFactory);
		int port = 21;
		try {
			port = Integer.parseInt(args[0]);
		} catch (Throwable e) {
			System.out.println("Port does not set, default 21");
		}
		server.bind(new InetSocketAddress("0.0.0.0", port));
	}
}

class EchoServerHandler extends SimpleChannelUpstreamHandler {
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Channel c = ctx.getChannel();
		System.out.print("Got message from (" + ctx.getChannel().getRemoteAddress() + "): " + e.getMessage());
		c.write(e.getMessage());
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		super.channelConnected(ctx, e);
		System.out.println("Connection established with (" + ctx.getChannel().getRemoteAddress() + ")");
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		super.channelDisconnected(ctx, e);
		System.out.println("Connection closed with (" + ctx.getChannel().getRemoteAddress() + ")");
	}
	
}


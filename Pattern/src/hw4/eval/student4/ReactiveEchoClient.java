package hw4.eval.student4;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

public class ReactiveEchoClient implements Runnable {
    private final int DEFAULT_SERVER_PORT = 8082;
    private ClientBootstrap bootstrap;
    private String message;

    public ReactiveEchoClient(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        Executor mainThreads = Executors.newCachedThreadPool();
        Executor workerThreads = Executors.newCachedThreadPool();
        ChannelFactory factory = new NioClientSocketChannelFactory(mainThreads,
                workerThreads);

        bootstrap = new ClientBootstrap(factory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();

                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("handler", new EchoClientHandler(message));

                return pipeline;
            }
        });

        ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(
                "localhost", DEFAULT_SERVER_PORT));
        System.out.println("ReactiveEchoClient: client [" + message
                + "] started");

        try {
            channelFuture.getChannel().getCloseFuture().await(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bootstrap.releaseExternalResources();
        System.out.println("ReactiveEchoClient: stop client [" + message + "]");
    }

    public static class EchoClientHandler extends SimpleChannelUpstreamHandler {
        private final ChannelBuffer messageContent;

        public EchoClientHandler(String input) {
            input += "\r\n";
            messageContent = ChannelBuffers.buffer(input.length());
            messageContent.writeBytes(input.getBytes());
            System.out.println("ReactiveEchoClient: sent message [" + input + "]");
        }

        @Override
        public void channelConnected(ChannelHandlerContext ctx,
                ChannelStateEvent e) {
            e.getChannel().write(messageContent);
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
            System.out.println("ReactiveEchoClient: received message ["
                    + e.getMessage() + "] from server");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            // Close the connection when an exception is raised.
            System.out.println(e.getCause());
            e.getChannel().close();
        }
    }

    // Init and start 10 client threads
    public static void main(String args[]) {
        for (int i = 0; i < 10; i++) {
            new Thread(new ReactiveEchoClient("client " + i)).start();
        }
    }
}

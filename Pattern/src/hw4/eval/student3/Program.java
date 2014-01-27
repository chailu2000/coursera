package hw4.eval.student3;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The synchronize transport interface.
 * This interface provides synchronize (blocked) version of transport
 * This is needed for Half-Sync part of realization
 */
interface SyncTransportInterface {
    /**
     * Read data from socket
     * It blocks until socket receives data or closed
     *
     * @return array of bytes, which are received from client
     */
    byte[] read();

    /**
     * Write data to socket.
     * It's a blocked call.
     *
     * @param data is bytes, which will be send to client
     */
    void write(byte[] data);
}

/**
 * This interface is used to process incoming data
 * There two implementations
 * Processing by chunks and processing by lines
 */
interface Processor extends Runnable {
}

/**
 * Queue between half-A-Sync and half-sync part of pattern
 */
class BlockingMultithreadBuffer {
    BlockingQueue queue = new LinkedBlockingQueue();
    AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Put part of data to buffer
     * @param data
     */
    void put(byte[] data) {
        try {
            queue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return  the data from buffer, if closed it return empty array
     */
    byte[] take() {
        if ( !closed.get())
        {
            try {
                return (byte[]) queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

    /**
     * Close the buffer.
     * It's happened
     */
    public void close() {
        // setup closed
        closed.set( true );
        // put the data, for thread, if it's waiting current
        put( new byte[0] );

    }
}

/**
 * Processor, which buffers input data and output this data by chunks
 * This class works synchronously.
 */
class ChunkProcessor implements Processor {

    final static int DEFAULT_CHUNK_SIZE = 12;

    List<Byte> buffer = new ArrayList<Byte>();

    private SyncTransportInterface transport;

    ChunkProcessor(SyncTransportInterface transport) {
        this.transport = transport;
    }

    /**
     * Take input and output by chunks
     */
    @Override
    public void run() {
        // run processing here
        byte[] data = transport.read();
        while (data.length > 0) {
            for (byte b : data) {
                buffer.add(b);
            }
            if (buffer.size() > DEFAULT_CHUNK_SIZE) {
                byte[] out = new byte[buffer.size()];
                for (int i = 0; i < buffer.size(); ++i) {
                    out[i] = buffer.get(i);
                }
                buffer.clear();
                transport.write(out);
            }
            data = transport.read();
        }
    }
}

/**
 * This class is a handler for Reactor pattern
 * This is Half-Async part of the pattern
 */
class EchoServerHandler extends SimpleChannelUpstreamHandler implements SyncTransportInterface {

    /**
     * The reactor socket channel .
     */
    private Channel channel;
    private BlockingMultithreadBuffer readbuffer = new BlockingMultithreadBuffer();

    EchoServerHandler() {
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // save channel for further using
        channel = ctx.getChannel();
        super.channelOpen(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        // Channel and ChannelBuffer is Wrapper Facade
        Channel c = e.getChannel();
        // Getting data without direct calling API, thus it's a part of Wrapper Facade pattern
        ChannelBuffer cb = (ChannelBuffer) e.getMessage();
        // put message to buffer queue
        readbuffer.put( cb.array() );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        e.getCause().printStackTrace();
        readbuffer.close();
        // close read buffer
        Channel ch = e.getChannel();
        ch.close();
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // close read buffer
        readbuffer.close();
        super.channelClosed(ctx, e);
    }

    /**
     * It's called from other thread and must be blocked until the data is arrived
     *
     * @return data, which is received from the client
     */
    @Override
    public byte[] read() {
        return readbuffer.take();
    }

    @Override
    public void write(byte[] data) {
        // check that channel is writable
        if (channel.isWritable() && data.length > 0) {
            channel.write(ChannelBuffers.wrappedBuffer(data));
        }
    }
}

/**
 * This class creates thread per connection.
 * It stores pool of threads and create threads per connection
 */
class HalfAsyncFactory implements ChannelPipelineFactory {
    private ExecutorService pool = Executors.newCachedThreadPool();

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        EchoServerHandler handler = new EchoServerHandler();
        // run thread, which is associated with
        pool.execute(new ChunkProcessor(handler));
        return Channels.pipeline(handler);
    }
}

/**
 * The server class, which configure reactor and run the server.
 */
class EchoServer {
    public void start(Integer port) {
        // configure the Reactor
        ChannelFactory factory = new NioServerSocketChannelFactory(
                Executors.newFixedThreadPool(2),
                Executors.newFixedThreadPool(2)
        );

        ServerBootstrap bootstrap = new ServerBootstrap(factory);

        bootstrap.setPipelineFactory(new HalfAsyncFactory());
        System.out.println("Port: " + port);
        // the Wrapper Facade pattern, no direct calling native API
        bootstrap.bind(new InetSocketAddress(port));
    }
}

/**
 * The main entry point
 */
public class Program {

    static final Integer DEFAULT_PORT = 8080;

    public static void main(String[] args) {

        Integer port = null;

        if (args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (port == null) {
            System.err.println("Port is not set. Using default port: " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        }

        EchoServer server = new EchoServer();
        server.start(port);
    }
}
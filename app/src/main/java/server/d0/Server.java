package server.d0;

import server.ChannelContext;
import server.ServerLogFormatter;
import server.ServerLogOptions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

public class Server
{
    public static final int PORT = 42069;
    private static final Logger log = ServerLogFormatter.getLogger(server.d1.Server.class);
    private static final ServerLogOptions logger = new ServerLogOptions(log);
    volatile static boolean isRunning = true;

    public static void main(String[] args)
    {
        new Server().startServer(PORT);
    }

    public static void acceptConnections(SelectionKey key) throws IOException
    {
        try // to acceptConnections
        {
            if (key.isValid())
            {
                if (key.isAcceptable())
                {
                    handleAccept(key);
                }
                if (key.isReadable())
                {
                    handleRead(key);
                }
                if (key.isWritable())
                {
                    handleWrite(key);
                }
            }
        }
        catch (Exception e)
        {
            logger.warning("Connection error: " + e.getMessage());
            key.cancel();
            key.channel()
               .close();
        }
    }


    private static void handleAccept(SelectionKey key)
    {
        ServerSocketChannel serverChannel;
        try
        {
            serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(key.selector(), SelectionKey.OP_READ, new ChannelContext(clientChannel));
            logger.info("Client connected to channel: " + clientChannel.socket().getInetAddress());
        }
        catch (Exception e)
        {
            logger.error("Error connecting client to server channel: " + e.getMessage());
            key.cancel();
        }
    }

    private static void closeChannel(SelectionKey key)
    {
        ChannelContext context = (ChannelContext) key.attachment();
        try
        {
            context.getChannel().socket().close();
            context.getChannel().close();
            key.channel().close();
            key.cancel();
            logger.warning("Client disconnected: " + context.getChannel().toString());
        }
        catch (Exception e)
        {
            logger.error(" " + e.getMessage());
        }
    }


    public static void handleWrite(SelectionKey key) throws IOException
    {
        ChannelContext context = (ChannelContext) key.attachment();
        String data = Charset.defaultCharset()
                             .decode(context.getWriteBuffer().flip())
                             .toString();
        logger.info("Response: \t" + data);

        context.getChannel().write(context.getWriteBuffer().flip());
        key.interestOps(SelectionKey.OP_READ);
    }

    public static void handleRead(SelectionKey key) throws Exception
    {
        ChannelContext context = (ChannelContext) key.attachment();
        ByteBuffer readByteBuffer = context.getReadBuffer();
        context.getWriteBuffer().clear();
        readByteBuffer.clear();

        int bytesRead = context.getChannel().read(readByteBuffer);
        if (bytesRead == -1)
        {
            closeChannel(key);
        } else {
        String data = Charset.defaultCharset()
                             .decode(readByteBuffer.flip())
                             .toString();
        logger.info("Request: \t" + data);

        context.getWriteBuffer().put(readByteBuffer.flip());
        key.interestOps(SelectionKey.OP_WRITE);
        }
    }


    public static void stopServer(Selector selector, ServerSocketChannel serverSocketChannel)
    {
        isRunning = false;
        logger.info("Shutting down...");

        try // to shut down server Gracefully
        {
            selector.wakeup(); // Unblocks select()
            for (SelectionKey key : selector.keys())
            {
                if (key.isValid()) key.channel()
                                      .close();
            }

            if (serverSocketChannel.isOpen())
            {
                serverSocketChannel.close();
                logger.warning("ServerSocketChannel is closed for server: " + serverSocketChannel.getLocalAddress());

            }

            if (selector.isOpen())
            {
                selector.close();
                logger.warning("Selector is closed for provider: " + selector.provider());
            }

            logger.info("Gracefully shutdown server.");
        }
        catch (IOException e)
        {
            System.out.println("Error during shutdown: " + e.getMessage());
        }
    }

    public void startServer(int port)
    {
        // to start the server
        try (
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()
        )
        {
            logger.info("Starting server on port: " + port);

            Selector selector = Selector.open();
            logger.info("Selector created for server: " + selector.provider());

            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket()
                               .bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            logger.info("ServerSocketChannel created for server: " + selector.provider());

            Runtime.getRuntime()
                   .addShutdownHook(new Thread(() -> stopServer(selector, serverSocketChannel)));
            logger.info("Graceful shutdown hook created for server: " + selector.provider());

            while (isRunning)
            {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext())
                {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    acceptConnections(key);
                }
            }
        }
        catch (Exception e)
        {
            logger.warning("Error starting server on port:" + port);
        }
    }
}

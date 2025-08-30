package server.d0;

import server.ChannelContext;
import server.ServerLogFormatter;

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
    private static final Logger logger = ServerLogFormatter.getLogger(Server.class);
    volatile static boolean isRunning = true;

    public static void main(String[] args)
    {
        new Server().startServer(PORT);
    }

    public static void acceptConnections(SelectionKey key) throws IOException
    {
        try // to acceptConnections
        {
            if (!key.isValid())
                return; // Skip invalid keys

            if (key.isAcceptable()) handleAccept(key);
            if (key.isValid()) if (key.isReadable()) handleRead(key);
            if (key.isValid()) if (key.isWritable()) handleWrite(key);
        }
        catch (Exception e)
        {
            logger.warning("Connection error: " + e.getMessage());
            key.cancel();
            key.channel()
               .close();
        }
    }

    private static void handleAccept(SelectionKey key) throws Exception
    {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(key.selector(), SelectionKey.OP_READ, new ChannelContext(clientChannel));
    }

    public static void handleWrite(SelectionKey key) throws IOException
    {
        ChannelContext context = (ChannelContext) key.attachment();
        ByteBuffer writeByteBuffer = context.getWriteBuffer();
        if (writeByteBuffer.hasRemaining())
        {
            context.getChannel()
                   .write(writeByteBuffer);

            writeByteBuffer.flip();
            String data = Charset.defaultCharset()
                                 .decode(writeByteBuffer)
                                 .toString();
//            logger.info("Response: \t" + data);

            writeByteBuffer.clear();
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    public static void handleRead(SelectionKey key)
    {
        try
        {
            ChannelContext context = (ChannelContext) key.attachment();
            ByteBuffer readByteBuffer = context.getReadBuffer();
            readByteBuffer.clear();

            int bytesRead = context.getChannel()
                                   .read(readByteBuffer);
            if (bytesRead == -1)
            {
                context.getChannel()
                       .close();
                key.cancel();
                return;
            }
            readByteBuffer.flip();

            String data = Charset.defaultCharset()
                                 .decode(readByteBuffer)
                                 .toString();
//            logger.info("Request: \t" + data);

            context.getWriteBuffer()
                   .put(data.getBytes());

            context.getWriteBuffer()
                   .flip();

            key.interestOps(SelectionKey.OP_WRITE);
        }
        catch (Exception e)
        {
            logger.warning("Read failed for channel: " + e.getMessage());
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
        try // to start the server
        {
            logger.info("Starting server on port: " + port);

            Selector selector = Selector.open();
            logger.info("Selector created for server: " + selector.provider());

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
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

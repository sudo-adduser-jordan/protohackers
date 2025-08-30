package server.d0;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import server.ServerLogFormatter;

public class Server
{
    private static final Logger logger = ServerLogFormatter.getLogger(Server.class);
    volatile static boolean isRunning = true;
    public static final int PORT = 42069;

    public static void main(String[] args)
    {
        new Server().startServer(PORT);
    }

    public void startServer(int port) 
    {
        try // to start the server
        {
            Selector selector = createSelector();
            ServerSocketChannel serverSocketChannel = createServerSocketChannel(port, selector);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> stopServer(selector, serverSocketChannel)));

            while (isRunning)
            {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext())
                {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    acceptConnections(selector, key, serverSocketChannel);
                }
            }
        }
        catch (Exception e)
        {
            logger.warning("Error starting server on port:" + port);
//            e.printStackTrace();
        }
    }


    public static Selector createSelector() throws IOException
    {
        logger.info("Server Selector created.");
        return Selector.open();
    }

    public static ServerSocketChannel createServerSocketChannel(int port, Selector selector) throws IOException
    {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        logger.info("Server SocketChannel created for provider: " + selector.provider());
        return serverSocketChannel;
    }

    public static void acceptConnections(Selector selector, SelectionKey key, ServerSocketChannel serverSocketChannel)
            throws IOException
    {
        try // to acceptConnections
        {
            if (key.isAcceptable())
            {
                SocketChannel clientChannel = createClientSocketChannel(serverSocketChannel);
                clientChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                logger.info("Accepted connection from " + clientChannel.getRemoteAddress());
            }

            if (key.isReadable()) {
                readChannel(key);
            };
        }
        catch (Exception e)
        {
            logger.warning("Connection error: " + e.getMessage());
            key.cancel();
            key.channel().close();
        }
    }


    public static SocketChannel createClientSocketChannel(ServerSocketChannel serverSocketChannel) throws IOException
    {
        SocketChannel clientSocketChannel = serverSocketChannel.accept();
        clientSocketChannel.configureBlocking(false);

        logger.info("SocketChannel created for client:" + clientSocketChannel.getRemoteAddress());
        return clientSocketChannel;
    }

    public static void writeChannel(SocketChannel clientSocketChannel, ByteBuffer responseByteBuffer) throws IOException
    {

        clientSocketChannel.write(responseByteBuffer);
//         logger.info("Response:\t" + message);
    }

    public static void readChannel(SelectionKey key) throws IOException
    {
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (clientSocketChannel.read(buffer) != -1)
        {
            writeChannel(clientSocketChannel, buffer);
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
                if (key.isValid()) key.channel().close();
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
//            e.printStackTrace();
        }
    }

}

package server.d2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import server.ServerLogFormatter;
import server.ServerLogOptions;

public class Server
{
    private static final Logger log = ServerLogFormatter.getLogger(Server.class);
    private static final ServerLogOptions logger = new ServerLogOptions(log);

    public static final int PORT = 42069;
    volatile static boolean isRunning = true;

    private static final int REQUEST_LENGTH = 9;
    private static final int RESPONSE_LENGTH = 4;
    private static final char INSERT_CHAR = 'I';
    private static final char QUERY_CHAR = 'Q';

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

            Runtime.getRuntime().addShutdownHook(new Thread(() ->
            {
                stopServer(selector, serverSocketChannel);
            }));

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
            e.printStackTrace();
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

    public void acceptConnections(Selector selector, SelectionKey key, ServerSocketChannel serverSocketChannel)
            throws IOException
    {
        try // to acceptConnections
        {
            if (key.isAcceptable())
            {
                SocketChannel clientChannel = createClientSocketChannel(serverSocketChannel);
                clientChannel.register(selector, SelectionKey.OP_READ, new SessionMemoryCache());
                logger.info("Accepted connection from " + clientChannel.getRemoteAddress());
            }
            
            if (key.isReadable())
            {
                Request request = readChannel(key);
                SessionMemoryCache sessionMemoryCache = (SessionMemoryCache) key.attachment();

                switch (request.getMessageType())
                {
                case INSERT -> {
                    sessionMemoryCache.addPrice(request.getFirstValue(), request.getSecondValue());
                    writeChannel(key, 73);
                }
                case QUERY -> {
                    int average = sessionMemoryCache.getAveragePriceInRange(request.getFirstValue(),
                    request.getSecondValue());
                    writeChannel(key, average);
                }
                }
            }
        }
        catch (Exception e)
        {
            key.cancel();
            key.channel().close();
            logger.warning("Connection error.");
            e.printStackTrace();
        }
    }

    public static SocketChannel createClientSocketChannel(ServerSocketChannel serverSocketChannel) throws IOException
    {
        SocketChannel clientSocketChannel = serverSocketChannel.accept();
        clientSocketChannel.configureBlocking(false);

        logger.info("SocketChannel created for client:" + clientSocketChannel.getRemoteAddress());
        return clientSocketChannel;
    }

    // Hexadecimal: 00 00 13 f3
    // Decoded: 5107 // intToBigEndianBytes
    public static void writeChannel(SelectionKey key, Integer value) throws IOException
    {
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        ByteBuffer responseBuffer = ByteBuffer.allocate(RESPONSE_LENGTH);

        if (value == null)
        {
            responseBuffer.putInt(0);
        }
        else
        {
            responseBuffer.putInt(value);
        }

        responseBuffer.flip();

        logger.info("Response: \t | " + responseBuffer.getInt(0)) ;
        clientSocketChannel.write(responseBuffer);

    }

    // Byte: | 0 | 1 2 3 4 | 5 6 7 8 |
    // Type: |char | int32 | int32 | // @Builder pattern
    public static Request readChannel(SelectionKey key) throws IOException
    {
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        ByteBuffer responseByteBuffer = ByteBuffer.allocate(REQUEST_LENGTH);
        Request.RequestBuilder builder = Request.builder();

        int bytesRead = clientSocketChannel.read(responseByteBuffer);
        if (bytesRead == -1)
        {
            clientSocketChannel.close();
            key.cancel();
            logger.warning("Connection closed by client: " + clientSocketChannel.socket().getInetAddress());
        }

        responseByteBuffer.flip();
        byte[] responseByteArray = responseByteBuffer.array();

        int firstValue = ByteBuffer.wrap(responseByteArray, 1, 4).order(ByteOrder.BIG_ENDIAN).getInt();
        int secondValue = ByteBuffer.wrap(responseByteArray, 5, 4).order(ByteOrder.BIG_ENDIAN).getInt();

        switch (responseByteArray[0])
        {
        case INSERT_CHAR:
            builder.MessageType(MessageTypes.INSERT);
            logger.debug("Request: \t | " + MessageTypes.INSERT + " " + firstValue + " " + secondValue);
            break;
        case QUERY_CHAR:
            builder.MessageType(MessageTypes.QUERY);
            logger.debug("Request: \t | " + MessageTypes.QUERY + " " + firstValue + " " + secondValue);
            break;
        default:
            logger.error("Bad request: " + new String(responseByteArray));
            key.channel().close();
            break;
        }

        return builder.FirstValue(firstValue).SecondValue(secondValue).build();
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
            e.printStackTrace();
        }
    }

}

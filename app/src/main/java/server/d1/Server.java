package server.d1;

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
    private static final Logger logger = ServerLogFormatter.getLogger(server.d0.Server.class);
    volatile static boolean isRunning = true;

    public static void main(String[] args)
    {
        new server.d0.Server().startServer(PORT);
    }

    public static void acceptConnections(SelectionKey key) throws IOException
    {
        if (!key.isValid())
        {
            try // to acceptConnections
            {
                if (key.isAcceptable()) handleAccept(key);
                if (key.isReadable()) handleRead(key);
                if (key.isWritable()) handleWrite(key);
            }
            catch (Exception e)
            {
                logger.warning("Connection error: " + e.getMessage());
                key.cancel();
                key.channel()
                   .close();
//                e.printStackTrace();
            }
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
        }
        if (!writeByteBuffer.hasRemaining())
        {
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
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket()
                               .bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            Runtime.getRuntime()
                   .addShutdownHook(new Thread(() -> stopServer(selector, serverSocketChannel)));

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
//import java.util.Objects;
//import java.util.Set;
//import java.util.logging.Logger;
//
//public class Server
//{
//    public static final int PORT = 42069;
//    private static final Logger logger = ServerLogFormatter.getLogger(Server.class);
//    volatile static boolean isRunning = true;
//    private static JsonMapper jsonMapper;
//
//    Server()
//    {
//        jsonMapper = JsonMapper.builder()
//                               .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
//                               .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//                               .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
//                               .build();
//    }
//
//    public static void main(String[] args)
//    {
//        new Server().startServer(PORT);
//    }
//
//    public static Selector createSelector() throws IOException
//    {
//        logger.info("Server Selector created.");
//        return Selector.open();
//    }
//
//    public static ServerSocketChannel createServerSocketChannel(int port, Selector selector) throws IOException
//    {
//        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
//        serverSocketChannel.configureBlocking(false);
//        serverSocketChannel.socket()
//                           .bind(new InetSocketAddress(port));
//        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
//
//        logger.info("Server SocketChannel created for provider: " + selector.provider());
//        return serverSocketChannel;
//    }
//
//    public static void acceptConnections(Selector selector, SelectionKey key, ServerSocketChannel serverSocketChannel) throws IOException
//    {
//        try // to acceptConnections
//        {
//
//            if (key.isAcceptable())
//            {
//                SocketChannel clientChannel = createClientSocketChannel(serverSocketChannel);
//                clientChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
//                logger.info("Accepted connection from " + clientChannel.getRemoteAddress());
//
//            }
//            if (key.isReadable())
//            {
//                String request = readChannel(key);
//                try // to validate json
//                {
//                    RequestJSON requestJSON = jsonMapper.readValue(request, RequestJSON.class);
//
//                    if (!Objects.equals(requestJSON.getMethod(), "isPrime"))
//                    {
//                        throw new Exception("method does not equal 'isPrime'");
//                    }
//                    boolean isPrime = isPrimeDouble(requestJSON.getNumber());
//                    ResponseJSON responseJSON = new ResponseJSON("isPrime", isPrime);
//                    writeChannel(key, jsonMapper.writeValueAsString(responseJSON));
//                }
//                catch (Exception e)
//                {
//                    writeChannel(key, Objects.requireNonNull(request));
//                }
//            }
//        }
//        catch (Exception e)
//        {
//            logger.warning("Connection error: " + e.getMessage());
//            key.cancel();
//            key.channel()
//               .close();
//        }
//    }
//
//    public static SocketChannel createClientSocketChannel(ServerSocketChannel serverSocketChannel) throws IOException
//    {
//        SocketChannel clientSocketChannel = serverSocketChannel.accept();
//        clientSocketChannel.configureBlocking(false);
//
//        logger.info("SocketChannel created for client:" + clientSocketChannel.getRemoteAddress());
//        return clientSocketChannel;
//    }
//
//    public static void writeChannel(SelectionKey key, String message) throws IOException
//    {
//        try (SocketChannel clientSocketChannel = (SocketChannel) key.channel())
//        {
//            ByteBuffer responseBuffer = ByteBuffer.wrap(message.getBytes());
//            while (responseBuffer.hasRemaining())
//            {
//                clientSocketChannel.write(responseBuffer);
//            }
//            responseBuffer.flip();
//            logger.info("Response:\t" + message);
//        }
//    }
//
//    public static String readChannel(SelectionKey key) throws IOException
//    {
//        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
//        ByteBuffer buffer = ByteBuffer.allocate(1024);
//
//        int bytesRead = clientSocketChannel.read(buffer);
//        if (bytesRead == -1)
//        {
//            clientSocketChannel.close();
//            key.cancel();
//            logger.warning("Connection closed by client: " + clientSocketChannel.socket()
//                                                                                .getInetAddress());
//            return null;
//        }
//
//        buffer.flip();
//        String message = new String(buffer.array(), 0, buffer.limit());
//        logger.info("Request: \t" + message);
//
//        buffer.clear();
//        return message;
//    }
//
//    private static boolean isPrimeDouble(double number)
//    {
//        if (number != Math.floor(number) || number <= 1) return false;
//        BigInteger bigInt = BigInteger.valueOf((long) number);
//        return bigInt.isProbablePrime(100);
//    }
//
//    public static void stopServer(Selector selector, ServerSocketChannel serverSocketChannel)
//    {
//        isRunning = false;
//        logger.info("Shutting down...");
//
//        try // to shut down server Gracefully
//        {
//            selector.wakeup(); // Unblocks select()
//            for (SelectionKey key : selector.keys())
//            {
//                if (key.isValid()) key.channel()
//                                      .close();
//            }
//
//            if (serverSocketChannel.isOpen())
//            {
//                serverSocketChannel.close();
//                logger.warning("ServerSocketChannel is closed for server: " + serverSocketChannel.getLocalAddress());
//
//            }
//
//            if (selector.isOpen())
//            {
//                selector.close();
//                logger.warning("Selector is closed for provider: " + selector.provider());
//            }
//
//            logger.info("Gracefully shutdown server.");
//        }
//        catch (IOException e)
//        {
//            System.out.println("Error during shutdown: " + e.getMessage());
//        }
//    }
//
//    public void startServer(int port)
//    {
//        try // to start the server
//        {
//            Selector selector = createSelector();
//            ServerSocketChannel serverSocketChannel = createServerSocketChannel(port, selector);
//
//            Runtime.getRuntime()
//                   .addShutdownHook(new Thread(() -> stopServer(selector, serverSocketChannel)));
//
//            while (isRunning)
//            {
//                selector.select();
//
//                Set<SelectionKey> selectedKeys = selector.selectedKeys();
//                Iterator<SelectionKey> iterator = selectedKeys.iterator();
//                while (iterator.hasNext())
//                {
//                    SelectionKey key = iterator.next();
//                    iterator.remove();
//                    acceptConnections(selector, key, serverSocketChannel);
//                }
//            }
//        }
//        catch (Exception e)
//        {
//            logger.warning("Error starting server on port:" + port);
//        }
//    }
//
//}

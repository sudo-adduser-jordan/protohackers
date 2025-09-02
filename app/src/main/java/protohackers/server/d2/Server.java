//
//
//package server.d2;
//
//import server.d1.ChannelContext;
//import protohackers.ServerLogFormatter;
//import protohackers.ServerLogOptions;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
//import java.util.Iterator;
//import java.util.Set;
//import java.util.logging.Logger;
//
//public class Server
//{
//    public static final int PORT = 42069;
//    private static final Logger log = protohackers.ServerLogFormatter.getLogger(Server.class);
//    private static final protohackers.ServerLogOptions logger = new protohackers.ServerLogOptions(log);
//    private static final int REQUEST_LENGTH = 9;
//    private static final int RESPONSE_LENGTH = 4;
//    private static final char INSERT_CHAR = 'I';
//    private static final char QUERY_CHAR = 'Q';
//    volatile static boolean isRunning = true;
//
//    public static void main(String[] args)
//    {
//        new Server().startServer(PORT);
//    }
//
//    public static void acceptConnections(SelectionKey key) throws IOException
//    {
//        try // to acceptConnections
//        {
//            if (!key.isValid())
//                return; // Skip invalid keys
//
//            if (key.isAcceptable()) handleAccept(key);
//            if (key.isReadable()) handleRead(key);
//            if (key.isValid()) if (key.isWritable()) handleWrite(key);
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
//    private static void handleAccept(SelectionKey key) throws Exception
//    {
//        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
//        SocketChannel clientChannel = serverChannel.accept();
//        clientChannel.configureBlocking(false);
//        clientChannel.register(key.selector(), SelectionKey.OP_READ, new ChannelContext(clientChannel));
//        logger.info("Client connected to channel: " + clientChannel.socket().getInetAddress());
//    }
//
//
//    // Hexadecimal: 00 00 13 f3
//    // Decoded: 5107 // send the byte
//    public static void handleWrite(SelectionKey key) throws IOException
//    {
//        ChannelContext context = (ChannelContext) key.attachment();
////
////        String data = Charset.defaultCharset()
////                             .decode(context.getWriteByteBuffer().flip())
////                             .toString();
//        logger.info("Response: \t" + context.getWriteByteBuffer().flip().getInt());
//
//        context.getChannel().write(context.getWriteByteBuffer().flip());
//        context.getWriteByteBuffer().clear();
//        key.interestOps(SelectionKey.OP_READ);
//    }
//
//    public static void handleRead(SelectionKey key) throws Exception
//    {
//        ChannelContext context = (ChannelContext) key.attachment();
//        ByteBuffer readByteBuffer = context.getReadByteBuffer();
//        readByteBuffer.clear();
//
//        int bytesRead = context.getChannel().read(readByteBuffer);
//        if (bytesRead == -1)
//        {
//            context.getChannel()
//                   .close();
//            key.cancel();
//            return;
//        }
//
//        readByteBuffer.flip();
//
//        Request.RequestBuilder builder = Request.builder();
//        byte[] responseByteArray = readByteBuffer.array();
//
//        int firstValue = ByteBuffer.wrap(responseByteArray, 1, 4)
//                                   .order(ByteOrder.BIG_ENDIAN)
//                                   .getInt();
//        int secondValue = ByteBuffer.wrap(responseByteArray, 5, 4)
//                                    .order(ByteOrder.BIG_ENDIAN)
//                                    .getInt();
//        switch (responseByteArray[0])
//        {
//            case INSERT_CHAR:
//                builder.MessageType(MessageTypes.INSERT);
//                logger.debug("Request: \t | " + MessageTypes.INSERT + " " + firstValue + " " + secondValue);
//                break;
//            case QUERY_CHAR:
//                builder.MessageType(MessageTypes.QUERY);
//                logger.debug("Request: \t | " + MessageTypes.QUERY + " " + firstValue + " " + secondValue);
//                break;
//            default:
//                logger.error("Bad request: " + new String(responseByteArray));
//                key.channel()
//                   .close();
//                break;
//        }
//
//        Request request = builder.FirstValue(firstValue)
//                                 .SecondValue(secondValue)
//                                 .build();
//
//        SessionMemoryCache sessionMemoryCache = context.getSessionMemoryCache();
//
//        context.getWriteByteBuffer().clear();
//        switch (request.getMessageType())
//        {
//            case INSERT ->
//            {
//                sessionMemoryCache.addPrice(request.getFirstValue(), request.getSecondValue());
//                context.getWriteByteBuffer().putInt(73);
//            }
//            case QUERY ->
//            {
//                int average = sessionMemoryCache.getAveragePriceInRange(request.getFirstValue(), request.getSecondValue());
//                context.getWriteByteBuffer().putInt(average);
//            }
//        }
//        key.interestOps(SelectionKey.OP_WRITE);
//    }
//
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
//            logger.info("Starting server on port: " + port);
//
//            Selector selector = Selector.open();
//            logger.info("Selector created for server: " + selector.provider());
//
//            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
//            serverSocketChannel.configureBlocking(false);
//            serverSocketChannel.socket()
//                               .bind(new InetSocketAddress(port));
//            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
//            logger.info("ServerSocketChannel created for server: " + selector.provider());
//
//            Runtime.getRuntime()
//                   .addShutdownHook(new Thread(() -> stopServer(selector, serverSocketChannel)));
//            logger.info("Graceful shutdown hook created for server: " + selector.provider());
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
//                    acceptConnections(key);
//                }
//            }
//        }
//        catch (Exception e)
//        {
//            logger.warning("Error starting server on port:" + port);
//        }
//    }
//}

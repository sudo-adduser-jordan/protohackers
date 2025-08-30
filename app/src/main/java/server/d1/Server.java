package server.d1;

import server.ChannelContext;
import server.ServerLogFormatter;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Objects;
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
            if (key.isReadable()) handleRead(key);
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

    private static boolean isPrimeDouble(double number)
    {
        if (number != Math.floor(number) || number <= 1) return false;
        BigInteger bigInt = BigInteger.valueOf((long) number);
        return bigInt.isProbablePrime(100);
    }

    private static void handleAccept(SelectionKey key) throws Exception
    {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(key.selector(), SelectionKey.OP_READ, new ChannelContext(clientChannel));
        logger.info("Client connected to channel: " + clientChannel.socket().getInetAddress());
    }

    public static void handleWrite(SelectionKey key) throws IOException
    {
        ChannelContext context = (ChannelContext) key.attachment();

        String data = Charset.defaultCharset()
                             .decode(context.getWriteBuffer().flip())
                             .toString();
        logger.info("Response: \t" + data);

        context.getWriteBuffer().clear();
        context.getChannel().write(context.getWriteBuffer().flip());
//        context.getChannel().write(context.getWriteBuffer().putChar('\n').flip());
        key.interestOps(SelectionKey.OP_READ);
    }


    public static void handleRead(SelectionKey key) throws Exception
    {
        ChannelContext context = (ChannelContext) key.attachment();
        ByteBuffer readByteBuffer = context.getReadBuffer();
        readByteBuffer.clear();

        int bytesRead = context.getChannel().read(readByteBuffer);
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
        logger.info("Request: \t" + data);

        try
        {
            RequestJSON requestJSON = context.getJsonMapper().readValue(data, RequestJSON.class);
            logger.info(requestJSON.toString());

            if (!Objects.equals(requestJSON.getMethod(), "isPrime"))
            {
                throw new Exception("method does not equal 'isPrime'");
            }
            boolean isPrime = isPrimeDouble(requestJSON.getNumber());
            ResponseJSON responseJSON = new ResponseJSON("isPrime", isPrime);

            context.getWriteBuffer().clear();
            context.getWriteBuffer().put(context.getJsonMapper().writeValueAsBytes(responseJSON));
        }
        catch (Exception e)
        {
            logger.warning("Invalid json: " + data);
            context.getWriteBuffer().clear();
            context.getWriteBuffer().put(readByteBuffer.flip());
        }
        key.interestOps(SelectionKey.OP_WRITE);
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



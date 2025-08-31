package server.d1;

import server.ChannelContext;
import server.ServerLogFormatter;
import server.ServerLogOptions;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
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

    private static boolean isPrimeDouble(double number)
    {
        if (number != Math.floor(number) || number <= 1) return false;
        BigInteger bigInt = BigInteger.valueOf((long) number);
        return bigInt.isProbablePrime(100);
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
            System.out.println("x");
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

    // WRITE
    public static void handleWrite(SelectionKey key) throws IOException
    {
        ChannelContext context = (ChannelContext) key.attachment();
        String response = Charset.defaultCharset().decode(context.getWriteBuffer().flip()).toString();
        logger.debug("Response:\t" + response);


        context.getChannel().write(context.getWriteBuffer().flip());

        key.interestOps(SelectionKey.OP_READ);
        context.getWriteBuffer().clear();
    }

    // READ
    public static void handleRead(SelectionKey key) throws Exception
    {
        ChannelContext context = (ChannelContext) key.attachment();
        context.getReadBuffer().clear();

        if ( -1 != context.getChannel().read(context.getReadBuffer()))
        {
            String requestString = Charset.defaultCharset().decode(context.getReadBuffer().flip()).toString().trim();



            logger.debug("Request: \t" + requestString);

            try // if valid json
            {


                if (requestString.length() > 100)
                {
                System.out.println(requestString.length());
                    context.getWriteBuffer().put(requestString.getBytes());
                    throw new Exception("sjflsjfs");
                }





                RequestJSON requestJSON = context.getJsonMapper()
                                                 .readValue(Charset.defaultCharset()
                                                                   .decode(context.getReadBuffer().flip())
                                                                   .toString(), RequestJSON.class);
                ResponseJSON responseJSON = new ResponseJSON("isPrime", isPrimeDouble(requestJSON.getNumber()));

                String responseString = context.getJsonMapper().writeValueAsString(responseJSON);

                if (requestString.contains("\n"))
                {
                    responseString += "\n";
                }

                context.getWriteBuffer().put(responseString.getBytes());
                key.interestOps(SelectionKey.OP_WRITE);
            }
            catch (Exception e)
            { // if not valid json
                context.getMessageBuffer().setLength(0);
                context.getMessageBuffer().append("close");
                context.getWriteBuffer().put(requestString.getBytes());
                key.interestOps(SelectionKey.OP_WRITE);
            }

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



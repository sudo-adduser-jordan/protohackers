package server.d1;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

class TestServer
{

    private static final int TEST_PORT = 6968;
    private static Thread serverThread;
    // private final int REQUESTS_PER_CLIENT = 10;
    private final int CLIENT_COUNT = 5;

    @BeforeAll
    public static void startServer() throws InterruptedException
    {
        serverThread = new Thread(() ->
                new Server().startServer(TEST_PORT));
        serverThread.start();
        Thread.sleep(250);
    }

    @AfterAll
    public static void stopServer() throws InterruptedException
    {
        Server.isRunning = false;
        serverThread.interrupt();
        serverThread.join(2000);
    }

    @RepeatedTest(CLIENT_COUNT)
    public void testValidJSONRequests()
    {
        for (int index = 0; index < CLIENT_COUNT; index++)
        {
            try (SocketChannel clientSocketChannel = SocketChannel.open())
            {
                clientSocketChannel.connect(new InetSocketAddress("localhost", TEST_PORT));
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);

                // without \n stay open
                clientSocketChannel.write(ByteBuffer.wrap(JSONRequests.validJSON.getBytes()));
                if (clientSocketChannel.read(readBuffer) == -1)
                {
                    System.out.println("read error");
                }
                String response = Charset.defaultCharset().decode(readBuffer.flip()).toString();

                Assertions.assertEquals(JSONRequests.validJSONResponse, response, "Response did not match request");

                // with \n close
                readBuffer.clear();
                clientSocketChannel.write(ByteBuffer.wrap((JSONRequests.validJSON + '\n').getBytes()));
                if (clientSocketChannel.read(readBuffer) != -1)
                {
                    response = Charset.defaultCharset().decode(readBuffer.flip()).toString();
                    Assertions.assertEquals(JSONRequests.validJSONResponse + '\n', response, "Response did not match request");
                }
            }
            catch (Exception e)
            {
                System.out.println("client connection failure");
            }
        }

    }

    @RepeatedTest(CLIENT_COUNT)
    public void testInvalidJSONRequests()
    {
        for (String message : JSONRequests.getInvalidJSONRequests())
        {
            try (SocketChannel clientSocketChannel = SocketChannel.open())
            {
                clientSocketChannel.connect(new InetSocketAddress("localhost", TEST_PORT));
                clientSocketChannel.write(ByteBuffer.wrap(message.getBytes()));

                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                if (clientSocketChannel.read(readBuffer) != -1)
                {
                    String response = Charset.defaultCharset().decode(readBuffer.flip()).toString();
                    Assertions.assertEquals(message + '\n', response, "Response did not match request: " + message);
                }
            }
            catch (Exception e)
            {
                System.out.println("client connection failure");
            }
        }
    }

}
package server.d1;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
    public void testValidJSONRequests() throws IOException
    {
        for (int index = 0; index < 5; index++)
        {
            SocketChannel clientSocketChannel = SocketChannel.open();
            clientSocketChannel.connect(new InetSocketAddress("localhost", TEST_PORT));

            ByteBuffer request = ByteBuffer.wrap(JSONRequests.validJSON.getBytes());
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);

            // without \n stay open
            clientSocketChannel.write(request);
            int bytesRead = clientSocketChannel.read(readBuffer);
            System.out.println(index);
            readBuffer.flip();

            byte[] bytes = new byte[bytesRead];
            readBuffer.get(bytes);
            String response = new String(bytes);

            Assertions.assertEquals(JSONRequests.validJSONResponse, response, "Response did not match request");

            // with \n close
            clientSocketChannel.write(request);
            bytesRead = clientSocketChannel.read(readBuffer);
            System.out.println(index);
            readBuffer.flip();

            bytes = new byte[bytesRead];
            readBuffer.get(bytes);
            response = new String(bytes);

            Assertions.assertEquals(JSONRequests.validJSONResponse, response, "Response did not match request");
            Assertions.assertFalse(clientSocketChannel.isOpen());

        }
    }

    @RepeatedTest(CLIENT_COUNT)
    public void testInvalidJSONRequests() throws IOException
    {
        for (String message : JSONRequests.getInvalidJSONRequests())
        {
            SocketChannel clientSocketChannel = SocketChannel.open();
            clientSocketChannel.connect(new InetSocketAddress("localhost", TEST_PORT));

            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            clientSocketChannel.write(buffer);

            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            int bytesRead = clientSocketChannel.read(readBuffer);
            readBuffer.flip();

            byte[] bytes = new byte[bytesRead];
            readBuffer.get(bytes);
            String response = new String(bytes);

            Assertions.assertEquals(message, response, "Response did not match request: " + message);
        }
    }

}
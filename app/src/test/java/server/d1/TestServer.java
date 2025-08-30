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
        SocketChannel client = SocketChannel.open();
        client.connect(new InetSocketAddress("localhost", TEST_PORT));
         client.configureBlocking(false);

        String message = JSONRequests.validJSON;
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        client.write(buffer);

        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        int bytesRead = client.read(readBuffer);
        readBuffer.flip();

        byte[] bytes = new byte[bytesRead];
        readBuffer.get(bytes);
        String response = new String(bytes);

        Assertions.assertEquals(JSONRequests.validJSONResponse, response, "Response did not match request");
    }

    @RepeatedTest(CLIENT_COUNT)
    public void testInvalidJSONRequests() throws IOException
    {
        SocketChannel client = SocketChannel.open();
        client.connect(new InetSocketAddress("localhost", TEST_PORT));
        client.configureBlocking(true);

        String[] inValidRequestJSON = JSONRequests.getInvalidJSONRequests();
        for (String message : inValidRequestJSON)
        {
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            client.write(buffer);

            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            int bytesRead = client.read(readBuffer);
            readBuffer.flip();

            byte[] bytes = new byte[bytesRead];
            readBuffer.get(bytes);
            String response = new String(bytes);

            Assertions.assertEquals(message, response, "Response did not match request: " + message);
        }
    }

}
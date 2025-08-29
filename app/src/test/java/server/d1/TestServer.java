package server.d1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.*;

class TestServer
{

    private static Thread serverThread;
    // private static final int REQUESTS_PER_CLIENT = 10;
    private static final int CLIENT_COUNT = 5;
    private final static int TEST_PORT = 6968;

    @BeforeAll
    public static void startServer() throws InterruptedException
    {
        serverThread = new Thread(() ->
        {
            try
            {
                new Server().startServer(TEST_PORT);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
        serverThread.start();
        serverThread.sleep(500);
    }

    @AfterAll
    public static void stopServer()
    {
        // Server.isRunning = false;
        serverThread.interrupt();
        try
        {
            serverThread.join(2000);
        }
        catch (InterruptedException ignored)

        {
        }
    }

    @RepeatedTest(CLIENT_COUNT)
    public void testValidJSONRequests() throws IOException
    {
        SocketChannel client = SocketChannel.open();
        client.connect(new InetSocketAddress("localhost", TEST_PORT));
        // client.configureBlocking(true);

        String message = JSONRequests.validJSON;
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        client.write(buffer);

        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        int bytesRead = client.read(readBuffer);
        readBuffer.flip();

        byte[] bytes = new byte[bytesRead];
        readBuffer.get(bytes);
        String response = new String(bytes);

        assertTrue(response.equals(JSONRequests.validJSONResponse), "Response did not match request");
    }

    @RepeatedTest(CLIENT_COUNT)
    public void testInvalidJSONRequests() throws IOException
    {
        SocketChannel client = SocketChannel.open();
        client.connect(new InetSocketAddress("localhost", TEST_PORT));
        client.configureBlocking(true);

        String[] inValidRequestJSON = JSONRequests.getInvalidJSONRequests();
        for (int i = 0; i < inValidRequestJSON.length; i++)
        {
            String message = inValidRequestJSON[i];
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            client.write(buffer);

            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            int bytesRead = client.read(readBuffer);
            readBuffer.flip();

            byte[] bytes = new byte[bytesRead];
            readBuffer.get(bytes);
            String response = new String(bytes);

            assertTrue(response.equals(message), "Response did not match request: " + message);
        }
    }

}
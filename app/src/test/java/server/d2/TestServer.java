package server.d2;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestServer
{

    private static Thread serverThread;
    private static final int REQUESTS_PER_CLIENT = 10;
    private static final int CLIENT_COUNT = 5;
    private static final int TEST_PORT = 6967;

    private String randomString(Random random, int length)
    {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
        {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

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
        Server.isRunning = false;
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
    public void testInsertRequests() throws IOException
    {
        try (SocketChannel client = SocketChannel.open())
        {
            client.connect(new InetSocketAddress("localhost", TEST_PORT));
            client.configureBlocking(true);

            for (int i = 1; i < REQUESTS_PER_CLIENT + 1; i++)
            {

                byte[] request = MessageBuilder.createInsertMessage(i, i * 4);
                ByteBuffer buffer = ByteBuffer.wrap(request);
                client.write(buffer);

                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int bytesRead = client.read(readBuffer);
                readBuffer.flip();
                // fail("Response is null", bytesRead == 0);

                byte[] response = new byte[bytesRead];
                readBuffer.get(response);
                // String response = new String(response);

                byte[] expected =
                { 0, 0, 0, 73 };

                assertArrayEquals(response, expected);
            }
        }
    }

    // @RepeatedTest(CLIENT_COUNT)
    // public void testQueryRequests() throws IOException {
    // try (SocketChannel client = SocketChannel.open()) {
    // client.connect(new InetSocketAddress("localhost", TEST_PORT));
    // client.configureBlocking(true);
    // Random random = new Random();

    // for (int i = 0; i < REQUESTS_PER_CLIENT; i++) {
    // String message = randomString(random, 16);
    // ByteBuffer buffer = ByteBuffer.wrap(MessageBuilder.createQueryMessage(1, 5));
    // client.write(buffer);

    // ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    // int bytesRead = client.read(readBuffer);
    // readBuffer.flip();

    // byte[] bytes = new byte[bytesRead];
    // readBuffer.get(bytes);
    // String response = new String(bytes);

    // assertTrue(response.equals(message), "Response did not match request");
    // }
    // }
    // }

}

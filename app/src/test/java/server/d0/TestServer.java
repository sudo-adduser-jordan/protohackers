package server.d0;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

public class TestServer
{

    private static Thread serverThread;
    private final int REQUESTS_PER_CLIENT = 10;
    private final int CLIENT_COUNT = 5;
    private  static final int TEST_PORT = 6969;

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
            new Server().startServer(TEST_PORT);
        });
        serverThread.start();
        Thread.sleep(250);
    }

    @AfterAll
    public static void stopServer()  throws InterruptedException
    {
        Server.isRunning = false;
        serverThread.interrupt();
        serverThread.join(2000);
    }

    @RepeatedTest(CLIENT_COUNT)
    public void testEchoRequests() throws IOException
    {
        try (SocketChannel client = SocketChannel.open())
        {
            client.connect(new InetSocketAddress("localhost", TEST_PORT));
            client.configureBlocking(true);
            Random random = new Random();

            for (int i = 0; i < REQUESTS_PER_CLIENT; i++)
            {
                String message = randomString(random, 16);
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                client.write(buffer);

                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int bytesRead = client.read(readBuffer);
                readBuffer.flip();

                byte[] bytes = new byte[bytesRead];
                readBuffer.get(bytes);
                String response = new String(bytes);

                Assertions.assertTrue(response.equals(message), "Response did not match request");
            }
        }
    }

}

package server.d0;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Random;

public class TestServer
{

    private static final int TEST_PORT = 6969;
    private static final int REQUESTS_PER_CLIENT = 10;
    private static Thread serverThread;
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

    private String randomString(Random random)
    {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++)
        {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
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
                String message = randomString(random);
                client.write(ByteBuffer.wrap(message.getBytes()));

                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                if(client.read(readBuffer) == -1)
                {
                    System.out.println("read error");
                }
                String response = Charset.defaultCharset().decode(readBuffer.flip()).toString();

                Assertions.assertEquals(response, message, "Response did not match request");
            }
        }
    }

}

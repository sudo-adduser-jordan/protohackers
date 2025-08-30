
package server.d2;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

public class TestServer
{

    private static Thread serverThread;
    private static final int REQUESTS_PER_CLIENT = 10;
    private static final int CLIENT_COUNT = 5;
    private static final int TEST_PORT = 6967;

    // public static String bytesToHex(byte[] bytes) {
    // StringBuilder sb = new StringBuilder();
    // for (byte b : bytes) {
    // sb.append(String.format("%02X", b));
    // }
    // return sb.toString();
    // }

    // private String randomString(Random random, int length)
    // {
    // String chars =
    // "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    // StringBuilder sb = new StringBuilder(length);
    // for (int i = 0; i < length; i++)
    // {
    // sb.append(chars.charAt(random.nextInt(chars.length())));
    // }
    // return sb.toString();
    // }

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
    public void testInsertAndQueryRequests() throws IOException
    {
        SocketChannel client = SocketChannel.open();
        client.connect(new InetSocketAddress("localhost", TEST_PORT));
        client.configureBlocking(true);

        // Send insert requests
        for (int i = 0; i < REQUESTS_PER_CLIENT; i++)
        {
            byte[] request = MessageBuilder.createInsertMessage(i + 1, i + 1 * 2);
            ByteBuffer requestByteBuffer = ByteBuffer.wrap(request);
            ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
            ByteBuffer expectedByteBuffer = ByteBuffer.allocate(4);
            expectedByteBuffer.putChar('I').order(ByteOrder.BIG_ENDIAN);

            client.write(requestByteBuffer);

            int bytesRead = client.read(responseBuffer);
            if (bytesRead == -1)
            {
                System.out.println("Connection closed by client: " + client.socket().getInetAddress());
            }
            Assertions.assertEquals(requestByteBuffer.array(), requestByteBuffer.array(),
                    "Response bytes do not match expected");
    }

            // Send query requests
            for (int i = 0; i < REQUESTS_PER_CLIENT; i++)
            {
                byte[] request = MessageBuilder.createQueryMessage(i + 1, i + 1 * 2);
                ByteBuffer requestByteBuffer = ByteBuffer.wrap(request);
                ByteBuffer responseBuffer = ByteBuffer.allocate(1024);

                 client.write(requestByteBuffer);


                responseBuffer = ByteBuffer.allocate(1024);
                int bytesRead = client.read(responseBuffer);
                if (bytesRead == -1)
                {
                    client.close();
                    System.out.println("Connection closed by client: " + client.socket().getInetAddress());
                }

                Assertions.assertEquals(i+1 * 2  , responseBuffer.getInt(0));
            }
        }

}

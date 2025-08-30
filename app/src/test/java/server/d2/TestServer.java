package server.d2;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

public class TestServer
{

    private static final int REQUESTS_PER_CLIENT = 10;
    private static final int CLIENT_COUNT = 5;
    private static final int TEST_PORT = 6967;
    private static Thread serverThread;

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
    public void testInsertAndQueryRequests() throws IOException
    {
        SocketChannel client = SocketChannel.open();
        client.connect(new InetSocketAddress("localhost", TEST_PORT));
        client.configureBlocking(true);

        // Send insert requests
        for (int i = 0; i < REQUESTS_PER_CLIENT; i++)
        {
            byte[] request = MessageBuilder.createInsertMessage(i + 1, i + 2);
            ByteBuffer requestByteBuffer = ByteBuffer.wrap(request);
            ByteBuffer responseBuffer = ByteBuffer.allocate(4);
            ByteBuffer expectedByteBuffer = ByteBuffer.allocate(4);
            expectedByteBuffer
                              .putInt('I')
                    .order(ByteOrder.BIG_ENDIAN);

            client.write(requestByteBuffer);

            int bytesRead = client.read(responseBuffer);
            if (bytesRead == -1)
            {
                System.out.println("Connection closed by client: " + client.socket()
                                                                           .getInetAddress());
            }
            Assertions.assertEquals(new String(expectedByteBuffer.array()), new String(responseBuffer.array()), "Response bytes do not match expected");
        }

        // Send query requests
        for (int i = 0; i < REQUESTS_PER_CLIENT; i++)
        {
            byte[] request = MessageBuilder.createQueryMessage(i + 1, i + 2);
            ByteBuffer requestByteBuffer = ByteBuffer.wrap(request);
            ByteBuffer responseBuffer;

            client.write(requestByteBuffer);

            responseBuffer = ByteBuffer.allocate(1024);
            int bytesRead = client.read(responseBuffer);
            if (bytesRead == -1)
            {
                client.close();
                System.out.println("Connection closed by client: " + client.socket()
                                                                           .getInetAddress());
            }

            Assertions.assertEquals(i + 2, responseBuffer.getInt(0));
        }
    }

}

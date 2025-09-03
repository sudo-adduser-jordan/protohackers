package protohackers.server.d2;

import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import protohackers.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

@Slf4j
public class TestServer
{
    private ArrayList<Connection> sockets;
    private static Thread serverThread;

    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    private static final int CLIENTS = 5;

    private static final int REQUEST_LENGTH = 9; // char, 2 ints
    private static final int RESPONSE_LENGTH = 4; // 1 int

    @BeforeAll // start a server
    public static void setUp()
    {
        serverThread = new Thread(() -> new Server().start(PORT));
        serverThread.start();
    }

    @AfterAll // close server
    public static void tearDownAll() throws InterruptedException
    {
        serverThread.join(1);
    }

    @BeforeEach // setup clients and context
    public void setUpEach() throws IOException
    {
        sockets = new ArrayList<>();
        for (int index = 0; index < CLIENTS; index++)
        {
            Connection socket = new Connection(new Socket(HOST, PORT));
            socket.getSocket().setSoTimeout(1000);
            sockets.add(socket);
        }
    }

    @AfterEach // close clients buffers and servers
    public void tearDown()
    {
        if (sockets != null) for (Connection socket : sockets) {socket.close();}
    }

    @Test
    void testAddAndGetPrice()
    {
        SessionMemoryCache cache = new SessionMemoryCache();
        cache.addPrice(1, 100);
        Assertions.assertEquals(100, cache.getPrice(1));
        Assertions.assertNull(cache.getPrice(2));
    }

    @Test
        // if returns a valid int   // mintime <= T <= maxtime
    void testAveragePriceInRange()
    {
        SessionMemoryCache sessionCache = new SessionMemoryCache();
        sessionCache.addPrice(1, 100);
        sessionCache.addPrice(3, 200);
        sessionCache.addPrice(5, 300);

        Assertions.assertEquals(200, sessionCache.getAveragePriceInRange(2, 4));
        Assertions.assertEquals(200, sessionCache.getAveragePriceInRange(1, 5));
        Assertions.assertEquals(0, sessionCache.getAveragePriceInRange(10, 20));
    }

    @Test // if valid responses
    public void testInsertTwoResponsesThenQuery() throws IOException
    {
        for (Connection socket : sockets)
        {
            try // to catch when server disconnects client
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(REQUEST_LENGTH);
                byteBuffer.put((byte) 'I');
                byteBuffer.putInt(100);
                byteBuffer.putInt(100);
                socket.getSocket().getOutputStream().write(byteBuffer.array());
                socket.getSocket().getOutputStream().flush();
                Assertions.assertFalse(socket.getSocket().isClosed());

                byteBuffer.clear();
                byteBuffer.put((byte) 'I');
                byteBuffer.putInt(300);
                byteBuffer.putInt(100);
                socket.getSocket().getOutputStream().write(byteBuffer.array());
                socket.getSocket().getOutputStream().flush();
                Assertions.assertFalse(socket.getSocket().isClosed());

                // Send query
                byteBuffer.clear();
                byteBuffer.put((byte) 'Q');
                byteBuffer.putInt(100);
                byteBuffer.putInt(200);
                byteBuffer.flip();
                socket.getSocket().getOutputStream().write(byteBuffer.array());
                socket.getSocket().getOutputStream().flush();

                ByteBuffer queryBuffer = ByteBuffer.wrap(socket.getSocket().getInputStream().readNBytes(4));
                Assertions.assertNotNull(queryBuffer);
                Assertions.assertEquals(4, queryBuffer.array().length);
                Assertions.assertEquals(100, queryBuffer.getInt());
            }
            catch (IOException e)
            {
                log.info("Client disconnected | {}", socket.getSocket().getInetAddress());
                Assertions.assertTrue(socket.getSocket().isClosed() || !socket.getSocket().isConnected());
            }
        }
    }

    class TestMessage
    {
        // Create an insert message (9 bytes)
        public static byte[] createInsertMessage(int timestamp, int price)
        {
            ByteBuffer buffer = ByteBuffer.allocate(9);
            buffer.put((byte) 'I'); // message type
            buffer.putInt(timestamp);
            buffer.putInt(price);
            return buffer.array();
        }

        // Create a query message (9 bytes)
        public static byte[] createQueryMessage(int minTime, int maxTime)
        {
            ByteBuffer buffer = ByteBuffer.allocate(9);
            buffer.put((byte) 'Q'); // message type
            buffer.putInt(minTime);
            buffer.putInt(maxTime);
            return buffer.array();
        }

        // Decode server response (4 bytes to int)
        public static int decodeResponse(byte[] response)
        {
            ByteBuffer buffer = ByteBuffer.wrap(response);
            buffer.order(ByteOrder.BIG_ENDIAN); // ensure big-endian
            return buffer.getInt();
        }
    }
}

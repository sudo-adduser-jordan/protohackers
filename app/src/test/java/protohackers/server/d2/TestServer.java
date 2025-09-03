package protohackers.server.d2;

import org.junit.jupiter.api.*;
import protohackers.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

public class TestServer
{
    private static final ServerLogOptions logger = new ServerLogOptions(ServerLogFormatter.getLogger(TestServer.class));
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
        if (sockets != null) for (Connection socket : sockets) socket.close();
    }

    @Test
    void testAddAndGetPrice() {
        SessionMemoryCache cache = new SessionMemoryCache();
        cache.addPrice(1, 100);
        Assertions.assertEquals(100, cache.getPrice(1));
        Assertions.assertNull(cache.getPrice(2));
    }

    @Test // if returns a valid int   // mintime <= T <= maxtime
    void testAveragePriceInRange() {
        SessionMemoryCache sessionCache= new SessionMemoryCache();
        sessionCache.addPrice(1, 100);
        sessionCache.addPrice(3, 200);
        sessionCache.addPrice(5, 300);

        Assertions.assertEquals(200, sessionCache.getAveragePriceInRange(2, 4));
        Assertions.assertEquals(200, sessionCache.getAveragePriceInRange(1, 5));
        Assertions.assertEquals(0, sessionCache.getAveragePriceInRange(10, 20));
    }

    @Test
    void testCreateInsertMessage() {
        int timestamp = 12345;
        int price = 678;
        byte[] message = TestMessage.createInsertMessage(timestamp, price);
        Assertions.assertEquals(9, message.length);
        Assertions.assertEquals((byte) 'I', message[0]);

        int extractedTimestamp = ByteBuffer.wrap(message, 1, 4).getInt();
        Assertions.assertEquals(timestamp, extractedTimestamp);

        int extractedPrice = ByteBuffer.wrap(message, 5, 4).getInt();
        Assertions.assertEquals(price, extractedPrice);
    }

    @Test
    void testCreateQueryMessage() {
        int minTime = 100;
        int maxTime = 200;
        byte[] message = TestMessage.createQueryMessage(minTime, maxTime);
        Assertions.assertEquals(9, message.length);
        Assertions.assertEquals((byte) 'Q', message[0]);

        int extractedMinTime = ByteBuffer.wrap(message, 1, 4).getInt();
        Assertions.assertEquals(minTime, extractedMinTime);

        int extractedMaxTime = ByteBuffer.wrap(message, 5, 4).getInt();
        Assertions.assertEquals(maxTime, extractedMaxTime);
    }

    @Test
    void testDecodeResponse() {
        int value = 123456789;
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(value);
        byte[] response = buffer.array();

        int decodedValue = TestMessage.decodeResponse(response);
        Assertions.assertEquals(value, decodedValue);
    }

    @Test // if valid responses
    public void testRequests()
    {
        for (Connection socket : sockets)
        {
            try // to catch when server disconnects client
            {
                socket.getWriter().println("xx" + "\n"); // w/ '\n'
                String response = socket.getReader().readLine();
                Assertions.assertNotNull(response);
                Assertions.assertEquals("xxx", response);
            }
            catch (IOException e)
            {
                logger.info("Client disconnected | " + socket.getSocket().getInetAddress());
                Assertions.assertTrue(socket.getSocket().isClosed() || !socket.getSocket().isConnected());
            }
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

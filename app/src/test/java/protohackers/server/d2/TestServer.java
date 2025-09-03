package protohackers.server.d1;

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

    private static final int HOST = "localhost";
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

    @Test // if integer is prime
    public void unitTestIsPrime()
    {
        Assertions.assertTrue(Server.isPrime(2), "2 should be prime");
        Assertions.assertTrue(Server.isPrime(13), "13 should be prime");
        Assertions.assertTrue(Server.isPrime(17), "17 should be prime");

        Assertions.assertFalse(Server.isPrime(1), "1 is not prime");
        Assertions.assertFalse(Server.isPrime(0), "0 is not prime");
        Assertions.assertFalse(Server.isPrime(-7), "Negative numbers are not prime");
        Assertions.assertFalse(Server.isPrime(4), "4 is not prime");
        Assertions.assertFalse(Server.isPrime(100), "100 is not prime");
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

    @Test // if valid response
    public void testValidJSON()
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

class MessageBuilder
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

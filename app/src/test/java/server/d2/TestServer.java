package server.d2;

import java.io.IOException;
import java.net.Socket;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestServer
{
    private final static int TEST_PORT = 6968;
    private static Server server;
    private Socket socket0;
    private Socket socket1;
    private Socket socket2;
    private Socket socket3;
    private Socket socket4;

    @BeforeAll
    public static void setup()
    {
        server = new Server();
        Thread serverThread = new Thread(() -> server.startServer(TEST_PORT));
        serverThread.start();
    }

    @BeforeEach 
    public void setupConnections()
    {
        try // to connect to server with multiple clients
        {
            socket0 = new Socket("localhost", TEST_PORT);
            socket1 = new Socket("localhost", TEST_PORT);
            socket2 = new Socket("localhost", TEST_PORT);
            socket3 = new Socket("localhost", TEST_PORT);
            socket4 = new Socket("localhost", TEST_PORT);

            assertTrue(!socket0.isClosed());
            assertTrue(!socket1.isClosed());
            assertTrue(!socket2.isClosed());
            assertTrue(!socket3.isClosed());
            assertTrue(!socket4.isClosed());
        }
        catch (IOException e)
        {
            fail("Failed to connect to server: " + e.getMessage());
        }
    }

    @Test
    public void testInsert() throws IOException
    {
    }

    @Test
    public void testQuery() throws IOException
    {
    }

    @AfterAll
    static void tearDown()
    {
        server.stopServer();
    }
}
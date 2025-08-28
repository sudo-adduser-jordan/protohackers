package server.d0;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestServer
{
    static int TEST_PORT = 6969;
    static Server server;

    @BeforeAll
    public static void setup()
    {
        server = new Server();
        Thread serverThread = new Thread(() -> server.startServer(TEST_PORT));
        serverThread.start();
    }

    @Test
    public void testSocketConnection()
    {
        try (Socket socket = new Socket("localhost", TEST_PORT))
        {
            assertTrue(socket.isConnected());
            socket.close();
        }
        catch (IOException e)
        {
            fail("Failed to connect to server: " + e.getMessage());
        }
    }

    @Test
    public void testSocketConnections()
    {
        try (Socket socket0 = new Socket("localhost", TEST_PORT);
                Socket socket1 = new Socket("localhost", TEST_PORT);
                Socket socket2 = new Socket("localhost", TEST_PORT);
                Socket socket3 = new Socket("localhost", TEST_PORT);
                Socket socket4 = new Socket("localhost", TEST_PORT);)
        {
            assertTrue(socket0.isConnected());
            assertTrue(socket1.isConnected());
            assertTrue(socket2.isConnected());
            assertTrue(socket3.isConnected());
            assertTrue(socket4.isConnected());

            socket0.close();
            socket1.close();
            socket2.close();
            socket3.close();
            socket4.close();
        }
        catch (IOException e)
        {
            fail("Failed to connect to server: " + e.getMessage());
        }
    }

    @Test
    public void testEchoRequests() throws IOException
    {
        try (Socket socket0 = new Socket("localhost", TEST_PORT);
                Socket socket1 = new Socket("localhost", TEST_PORT);
                Socket socket2 = new Socket("localhost", TEST_PORT);
                Socket socket3 = new Socket("localhost", TEST_PORT);
                Socket socket4 = new Socket("localhost", TEST_PORT);)
        {
            String request = "message echo bytes";
            Socket[] sockets =
            { socket0, socket1, socket2, socket3, socket4 };

            for (Socket socket : sockets)
            {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                ByteArrayOutputStream response = new ByteArrayOutputStream();

                outputStream.write(request.getBytes());
                socket.shutdownOutput();

                int readBytes;
                byte[] buffer = new byte[1024];
                while ((readBytes = inputStream.read(buffer)) != -1)
                {
                    response.write(buffer, 0, readBytes);
                }

                assertEquals(request, response.toString(), "Reponse mismatched for socket: " + socket);
                socket.close();
            }
        }
    }

    @AfterAll
    static void tearDown()
    {
        server.stopServer();
    }

}
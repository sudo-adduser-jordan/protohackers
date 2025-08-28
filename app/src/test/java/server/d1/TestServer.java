package server.d1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
        (Socket socket0 = new Socket("localhost", TEST_PORT);
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
    public void testValidJSONRequests() throws IOException
    {
        try // to connect to server with multiple clients
        (Socket socket0 = new Socket("localhost", TEST_PORT);
                Socket socket1 = new Socket("localhost", TEST_PORT);
                Socket socket2 = new Socket("localhost", TEST_PORT);
                Socket socket3 = new Socket("localhost", TEST_PORT);
                Socket socket4 = new Socket("localhost", TEST_PORT);)
        {

            Socket[] sockets =
            { socket0, socket1, socket2, socket3, socket4 };

            for (Socket socket : sockets)
            {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(outputStream));

                output.write(JSONRequests.validJSON);
                output.newLine();
                output.flush();

                assertEquals(input.readLine(), JSONRequests.validJSONResponse);

                socket.close();
            }
        }
        catch (IOException e)
        {
            fail("Failed to connect to server: " + e.getMessage());
        }
    }

    @Test
    public void testInvalidJSONRequests() throws IOException
    {
        try // to connect to server with multiple clients
        (Socket socket0 = new Socket("localhost", TEST_PORT);
                Socket socket1 = new Socket("localhost", TEST_PORT);
                Socket socket2 = new Socket("localhost", TEST_PORT);
                Socket socket3 = new Socket("localhost", TEST_PORT);
                Socket socket4 = new Socket("localhost", TEST_PORT);)
        {
            for (String jsonRequest : JSONRequests.getInvalidJSONRequests())
            {
                Socket socket = new Socket("localhost", TEST_PORT);
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                output.write(jsonRequest);
                output.newLine();
                output.flush();

                assertEquals(input.readLine(), jsonRequest);

                socket.close();
            }
        }
        catch (IOException e)
        {
            fail("Failed to connect to server: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDown()
    {
        server.stopServer();
    }
}
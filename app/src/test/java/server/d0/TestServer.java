package server.d0;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestServer 
{
        static int TEST_PORT = 6969;
        static Server server;
        Socket socket0;
        Socket socket1;
        Socket socket2;
        Socket socket3;
        Socket socket4;

@BeforeAll
public static void setup() {
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
    } catch (IOException e) {
        fail("Failed to connect to server: " + e.getMessage());
    }
}

@Test
public void testSocketConnections() 
{
    try (
        Socket socket0 = new Socket("localhost", TEST_PORT);
        Socket socket1 = new Socket("localhost", TEST_PORT);
        Socket socket2 = new Socket("localhost", TEST_PORT);
        Socket socket3 = new Socket("localhost", TEST_PORT);
        Socket socket4 = new Socket("localhost", TEST_PORT);
    ) {
        assertTrue(socket0.isConnected());
        assertTrue(socket1.isConnected());
        assertTrue(socket2.isConnected());
        assertTrue(socket3.isConnected());
        assertTrue(socket4.isConnected());
    } catch (IOException e) {
        fail("Failed to connect to server: " + e.getMessage());
    }
}

@Test
public void testEchoRequests() throws IOException {
    String message = "echo message";
        socket0 = new Socket("localhost", TEST_PORT);
        socket1 = new Socket("localhost", TEST_PORT);
        socket2 = new Socket("localhost", TEST_PORT);
        socket3 = new Socket("localhost", TEST_PORT);
        socket4 = new Socket("localhost", TEST_PORT);

    Socket[] sockets = {socket0, socket1, socket2, socket3, socket4};

    for (Socket socket : sockets) {
        socket.getOutputStream().write(message.getBytes());
        socket.shutdownOutput();
    }

    for (Socket socket : sockets) {
        InputStream input = socket.getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        int readBytes;
        byte[] buffer = new byte[1024];

        while ((readBytes = input.read(buffer)) != -1) {
            output.write(buffer, 0, readBytes);
        }

        assertEquals(message, output.toString(), "Echoed message mismatch for socket: " + socket);
        socket.close();
    }
}

@AfterAll
static void tearDown() {
    server.stopServer();
}

}
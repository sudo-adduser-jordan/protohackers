package server.d0;


import java.io.IOException;
import java.net.Socket;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestServer 
{
static Server server;

@BeforeAll
public static void setup() {
    server = new Server();
    Thread serverThread = new Thread(() -> server.startServer());
    serverThread.start();

    // Thread.sleep(100); // tune as needed
}


@Test
public void testSocketConnection() 
{
try (Socket socket = new Socket("localhost", 42069)) 
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
        Socket socket = new Socket("localhost", 42069);
        Socket socket1 = new Socket("localhost", 42069);
        Socket socket2 = new Socket("localhost", 42069);
        Socket socket3 = new Socket("localhost", 42069);
        Socket socket4 = new Socket("localhost", 42069);
    ) {
        assertTrue(socket.isConnected());
        assertTrue(socket1.isConnected());
        assertTrue(socket2.isConnected());
        assertTrue(socket3.isConnected());
        assertTrue(socket4.isConnected());
    } catch (IOException e) {
        fail("Failed to connect to server: " + e.getMessage());
    }
}


@AfterAll
static void tearDown() {
    server.stopServer();
}


}
package server.d1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestServer
{
    
    private static Thread serverThread;
    private static final int REQUESTS_PER_CLIENT = 10;
    private static final int CLIENT_COUNT = 5;
    private final static int TEST_PORT = 6968;


    @BeforeAll
    public static void startServer() {
        serverThread = new Thread(() -> {
            // try {
                new Server().startServer(TEST_PORT);
            // } catch (IOException e) {
                // e.printStackTrace();
            // }
        });
        serverThread.start();
    }

    @AfterAll
    public static void stopServer() {
        // Server.isRunning = false;
        serverThread.interrupt();
        try {
            serverThread.join(2000);
        } catch (InterruptedException ignored) {
        }
    }

    // @Test
    // public void testValidJSONRequests() throws IOException
    // {
    //     try // to connect to server with multiple clients
    //     (Socket socket0 = new Socket("localhost", TEST_PORT);
    //             Socket socket1 = new Socket("localhost", TEST_PORT);
    //             Socket socket2 = new Socket("localhost", TEST_PORT);
    //             Socket socket3 = new Socket("localhost", TEST_PORT);
    //             Socket socket4 = new Socket("localhost", TEST_PORT);)
    //     {

    //         Socket[] sockets =
    //         { socket0, socket1, socket2, socket3, socket4 };

    //         for (Socket socket : sockets)
    //         {
    //             InputStream inputStream = socket.getInputStream();
    //             OutputStream outputStream = socket.getOutputStream();
    //             BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
    //             BufferedWriter output = new BufferedWriter(new OutputStreamWriter(outputStream));

    //             output.write(JSONRequests.validJSON);
    //             output.newLine();
    //             output.flush();

    //             assertEquals(input.readLine(), JSONRequests.validJSONResponse);

    //             socket.close();
    //         }
    //     }
    //     catch (IOException e)
    //     {
    //         fail("Failed to connect to server: " + e.getMessage());
    //     }
    // }

    // @Test
    // public void testInvalidJSONRequests() throws IOException
    // {
    //     try // to connect to server with multiple clients
    //     (Socket socket0 = new Socket("localhost", TEST_PORT);
    //             Socket socket1 = new Socket("localhost", TEST_PORT);
    //             Socket socket2 = new Socket("localhost", TEST_PORT);
    //             Socket socket3 = new Socket("localhost", TEST_PORT);
    //             Socket socket4 = new Socket("localhost", TEST_PORT);)
    //     {
    //         for (String jsonRequest : JSONRequests.getInvalidJSONRequests())
    //         {
    //             Socket socket = new Socket("localhost", TEST_PORT);
    //             BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    //             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    //             output.write(jsonRequest);
    //             output.newLine();
    //             output.flush();

    //             assertEquals(input.readLine(), jsonRequest);

    //             socket.close();
    //         }
    //     }
    //     catch (IOException e)
    //     {
    //         fail("Failed to connect to server: " + e.getMessage());
    //     }
    // }

    @RepeatedTest(CLIENT_COUNT)
    public void testValidJSONRequests() throws IOException {
        try (SocketChannel client = SocketChannel.open()) {
            client.connect(new InetSocketAddress("localhost", Server.PORT));
            client.configureBlocking(true);
            Random random = new Random();

            for (int i = 0; i < REQUESTS_PER_CLIENT; i++) {
                String message = randomString(random, 16);
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                client.write(buffer);

                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int bytesRead = client.read(readBuffer);
                readBuffer.flip();

                byte[] bytes = new byte[bytesRead];
                readBuffer.get(bytes);
                String response = new String(bytes);

                assertTrue(response.equals(message), "Response did not match request");
            }
        }
    }
        @RepeatedTest(CLIENT_COUNT)
    public void testInvalidJSONRequests() throws IOException {
        try (SocketChannel client = SocketChannel.open()) {
            client.connect(new InetSocketAddress("localhost", Server.PORT));
            client.configureBlocking(true);
            Random random = new Random();

            for (int i = 0; i < REQUESTS_PER_CLIENT; i++) {
                String message = randomString(random, 16);
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                client.write(buffer);

                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int bytesRead = client.read(readBuffer);
                readBuffer.flip();

                byte[] bytes = new byte[bytesRead];
                readBuffer.get(bytes);
                String response = new String(bytes);

                assertTrue(response.equals(message), "Response did not match request");
            }
        }
    }

}
package server.d0;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestServer {

    private static Thread serverThread;
    private static final int REQUESTS_PER_CLIENT = 10;
    private static final int CLIENT_COUNT = 5;

    private String randomString(Random random, int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }


    @BeforeAll
    public static void startServer() {
        serverThread = new Thread(() -> {
            try {
                new Server().startServer(Server.PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    @AfterAll
    public static void stopServer() {
        Server.isRunning = false;
        serverThread.interrupt();
        try {
            serverThread.join(2000);
        } catch (InterruptedException ignored) {
        }
    }

    @RepeatedTest(CLIENT_COUNT)
    public void testEchoRequests() throws IOException {
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

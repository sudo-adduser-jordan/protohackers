package server.d2;

import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    }

    @Test
    public void testInsert() throws IOException
    {
    }

    @Test
    public void testQuery() throws IOException
    {
    }

    @Test
    public void testQueryReturnsZero()
    {
        // Query with no prior insertions
        // byte[] queryMsg = ClientMessageBuilder.createQueryMessage(0, 100);
        // byte[] response = server.processMessage(queryMsg);
        // int result = ClientMessageBuilder.decodeResponse(response);
        // assertEquals(0, result);
    }

    @Test
    public void testInvalidMessageLengthThrows()
    {
        // byte[] invalidMsg = new byte[5]; // too short
        // try
        // {
        // server.processMessage(invalidMsg);
        // fail("Should throw IllegalArgumentException");
        // }
        // catch (IllegalArgumentException e)
        // {
        // // expected
        // }
    }

    @Test
    public void testInsertAndQuery_ReturnsExpectedAverage()
    {
        // Insert multiple data points
        // server.processMessage(MessageBuilder.createInsertMessage(12345, 101));
        // server.processMessage(MessageBuilder.createInsertMessage(12346, 102));
        // server.processMessage(MessageBuilder.createInsertMessage(12347, 100));
        // server.processMessage(MessageBuilder.createInsertMessage(40960, 5));

        // Query between timestamps 12288 and 16384
        // byte[] queryMsg = MessageBuilder.createQueryMessage(12288, 16384);
        // byte[] response = server.processMessage(queryMsg);

        // int average = MessageBuilder.decodeResponse(response);

        // Expect the average of 101, 102, 100 within that range: (101+102+100)/3 = 101
        // assertEquals(101, average);
    }

    @AfterAll
    static void tearDown()
    {
        server.stopServer();
    }
}
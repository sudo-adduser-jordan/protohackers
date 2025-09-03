package protohackers.server.d1;

import org.junit.jupiter.api.*;
import protohackers.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class TestServer
{
    private static final int CLIENTS = 5;
    private static final int PORT = 12345;
    private static final String HOST = "localhost";
    private static final ServerLogOptions logger = new ServerLogOptions(ServerLogFormatter.getLogger(TestServer.class));

    private static Thread serverThread;
    private ArrayList<Connection> sockets;

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
                // socket.getWriter().println(JSONRequests.validJSON); //
                socket.getWriter().println(JSONRequests.validJSON + "\n"); // w/ '\n'
                String response = socket.getReader().readLine();
                Assertions.assertNotNull(response);
                Assertions.assertEquals(JSONRequests.validJSONResponse, response);
            }
            catch (IOException e)
            {
                logger.info("Client disconnected | " + socket.getSocket().getInetAddress());
                Assertions.assertTrue(socket.getSocket().isClosed() || !socket.getSocket().isConnected());
            }
        }
    }

    @Test // if invalid response
    public void testInValidJSON() throws IOException
    {
        tearDown(); // unused sockets that will time out

        for (String inValidJSON : JSONRequests.getInvalidJSONRequests())
        {
            for (int index = 0; index < CLIENTS; index++)
            {
                Connection socket = new Connection(new Socket(HOST, PORT));
                socket.getSocket().setSoTimeout(1000);

                try // to catch when server disconnects client
                {
                    socket.getWriter().println(inValidJSON); // send message

                    String response = socket.getReader().readLine();
                    Assertions.assertNotNull(response);
                    Assertions.assertEquals(inValidJSON, response);
                }
                catch (IOException e)
                {
                    logger.info("Client disconnected | " + socket.getSocket().getInetAddress());
                    Assertions.assertTrue(socket.getSocket().isClosed() || !socket.getSocket().isConnected());
                }
            }
        }
    }

}

class JSONRequests
{
    // Valid JSON string
    public static final String validJSON = """
            {"method":"isPrime","number":123}""";

    public static final String validJSONResponse = """
            {"method":"isPrime","prime":false}""";

    // Invalid JSON strings
    public static final String invalidJSONMissingColon = """
            {"method":"isPrime","number"123}""";

    public static final String invalidJSONTrailingComma = """
            {"method":"isPrime","number":123,}""";

    public static final String invalidJSONUnclosedBrace = """
            {"method":"isPrime","number":123""";

    public static final String invalidJSONExtraComma = """
            {"method":"isPrime","number":123,}""";

    public static final String invalidJSONMissingQuotes = """
            {method:"isPrime","number":123}""";

    public static final String invalidJSONWrongQuotes = """
            {'method':'isPrime','number':123}""";

    public static final String invalidJSONEmptyObject = """
            {}""";

    public static final String invalidJSONEmptyArray = """
            []""";

    public static final String invalidJSONNumberAsString = """
            {"method":"isPrime","number":"123"}""";

    public static final String invalidJSONExtraField = """
            {"method":"isPrime","number":123,"extra":"field"}""";

    public static final String invalidJSONMissingMethod = """
            {"number":123}""";

    public static final String invalidJSONMissingNumber = """
            {"method":"isPrime"}""";

    public static final String invalidJSONIncorrectType = """
            {"method":"isPrime","number":"notANumber"}""";

    public static final String invalidJSONArrayInsteadOfObject = """
            ["method","isPrime","number",123]""";

    public static final String invalidJSONNestedMalformed = """
            {"method":"isPrime","parameters":{"number":123}""";

    public static String[] getInvalidJSONRequests()
    {
        return new String[]{invalidJSONMissingColon, invalidJSONTrailingComma,
                invalidJSONUnclosedBrace,
                invalidJSONExtraComma, invalidJSONMissingQuotes, invalidJSONWrongQuotes, invalidJSONEmptyObject, invalidJSONEmptyArray, invalidJSONNumberAsString,
//                 invalidJSONExtraField,
                invalidJSONMissingMethod, invalidJSONMissingNumber, invalidJSONIncorrectType, invalidJSONArrayInsteadOfObject, invalidJSONNestedMalformed};
    }

}
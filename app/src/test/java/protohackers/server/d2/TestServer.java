//package protohackers.server.d1;
//
//import org.junit.jupiter.api.*;
//import protohackers.server.Context;
//
//import java.io.IOException;
//import java.net.Socket;
//import java.util.ArrayList;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//public class TestServer
//{
//    private static final int CLIENTS = 5;
//    private static final int PORT = 12345;
//    private static final String HOST = "localhost";
//
//    private static Thread serverThread;
//    private ArrayList<Context> sockets;
//
//    @BeforeAll // start a server
//    public static void setUp()
//    {
//        serverThread = new Thread(() -> new Server().start(PORT));
//        serverThread.start();
//    }
//
//    @BeforeEach // setup client context
//    public void setUpEach() throws IOException
//    {
//        sockets = new ArrayList<>();
//        for (int i = 0; i < CLIENTS; i++)
//        {
//            Context socket = new Context(new Socket(HOST, PORT));
//            sockets.add(socket);
//        }
//    }
//
//    @AfterEach // close clients buffers and servers
//    public void tearDown() throws IOException, InterruptedException
//    {
//        for (Context socket : sockets)
//        {
//            socket.close();
//        }
//        serverThread.join();
//    }
//
//    @Test // if integer is prime
//    public void unitTestIsPrime()
//    {
//        assertTrue(Server.isPrime(2), "2 should be prime");
//        assertTrue(Server.isPrime(13), "13 should be prime");
//        Assertions.assertFalse(Server.isPrime(1), "1 is not prime");
//        Assertions.assertFalse(Server.isPrime(0), "0 is not prime");
//        Assertions.assertFalse(Server.isPrime(-7), "Negative numbers are not prime");
//        Assertions.assertFalse(Server.isPrime(4), "4 is not prime");
//        assertTrue(Server.isPrime(17), "17 should be prime");
//        Assertions.assertFalse(Server.isPrime(100), "100 is not prime");
//    }
//
//    @Test // if valid response
//    public void testValidJSON() throws IOException
//    {
//        for (Context socket : sockets)
//        {
//            socket.getWriter().println(JSONRequests.validJSON);
//            String response = socket.getReader().readLine();
//            Assertions.assertNotNull(response);
//            Assertions.assertEquals(JSONRequests.validJSONResponse, response);
//        }
//    }
//
//    @Test // if valid response
//    public void testInvalidJSON() throws IOException
//    {
//        for (Context socket : sockets)
//        {
//            socket.getWriter().println(JSONRequests.validJSON);
//            String response = socket.getReader().readLine();
//            Assertions.assertNotNull(response);
//            Assertions.assertEquals(JSONRequests.validJSONResponse, response);
//        }
//    }
//}
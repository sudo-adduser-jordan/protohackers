package protohackers.server.d2;

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

//
//    @Test // if the correct average is returned
//    public void unitTestGetAverage()
//    {
//    }

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
        if (sockets != null) for (Connection socket : sockets) {socket.close();}
    }

//    @Test // if valid response
//    public void testValidJSON()
//    {
//        for (Connection socket : sockets)
//        {
//            try // to catch when server disconnects client
//            {
//                // socket.getWriter().println(JSONRequests.validJSON); //
//                socket.getWriter().println(JSONRequests.validJSON + "\n"); // w/ '\n'
//                String response = socket.getReader().readLine();
//                Assertions.assertNotNull(response);
//                Assertions.assertEquals(JSONRequests.validJSONResponse, response);
//            }
//            catch (IOException e)
//            {
//                logger.info("Client disconnected | " + socket.getSocket().getInetAddress());
//                Assertions.assertTrue(socket.getSocket().isClosed() || !socket.getSocket().isConnected());
//            }
//        }
//    }
//
//
//    @Test // if invalid response
//    public void testInValidJSON() throws IOException
//    {
//        tearDown(); // unused sockets that will time out
//
//        for (String inValidJSON : JSONRequests.getInvalidJSONRequests())
//        {
//            for (int index = 0; index < CLIENTS; index++)
//            {
//                Connection socket = new Connection(new Socket(HOST, PORT));
//                socket.getSocket().setSoTimeout(1000);
//
//                try // to catch when server disconnects client
//                {
//                    socket.getWriter().println(inValidJSON); // send message
//
//                    String response = socket.getReader().readLine();
//                    Assertions.assertNotNull(response);
//                    Assertions.assertEquals(inValidJSON, response);
//                }
//                catch (IOException e)
//                {
//                    logger.info("Client disconnected | " + socket.getSocket().getInetAddress());
//                    Assertions.assertTrue(socket.getSocket().isClosed() || !socket.getSocket().isConnected());
//                }
//            }
//        }
//    }

}

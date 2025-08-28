package server.d0;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import server.ServerLogFormatter;

public class Server
{
    private static final Logger logger = ServerLogFormatter.getLogger(Server.class);
    private static final int MAX_THREADS = 5;
    private static final int PORT = 42069;

    private final ExecutorService executorService;
    private static ServerSocket serverSocket;

    public Server()
    {
        logger.info("Server created.");
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
        logger.info("ExecutorService created with FixedThreadPool: 5");
    }

    public static void main(String[] args)
    {
        Server server = new Server();
        server.startServer(PORT);
    }

    public void startServer(int port)
    {
        try // to initializeServerSocket
        {
            initializeServerSocket(port);
            waitForConnections();
        }
        catch (IOException ex)
        {
            logger.warning("Error starting server: " + ex.getMessage());
        }
        finally
        {
            stopServer();
        }
    }

    private void initializeServerSocket(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(70000);
        logger.info("Server connected to port: " + port);
        logger.info("Server timeout set: " + serverSocket.getSoTimeout());
    }

    private void waitForConnections() throws IOException
    {
        while (!serverSocket.isClosed())
        {
            try // try to accept socket connections
            {
                Socket clientSocket = serverSocket.accept();
                executorService.execute(() -> handleClientSocket(clientSocket));
            }
            catch (IOException ex)
            {
                logger.warning("Error accepting connection: " + ex.getMessage());
            }
        }
    }

    public void handleClientSocket(Socket clientSocket)
    {
        new ServerRunnable(clientSocket).run();
    }

    public void stopServer()
    {
        executorService.shutdown();
    }

}

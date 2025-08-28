package server.d2;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import server.ServerLogFormatter;
import server.ServerLogOptions;

public class Server
{
    private static final Logger log = ServerLogFormatter.getLogger(Server.class);
    private static final ServerLogOptions logger = new ServerLogOptions(log);
    
    private static final int MAX_THREADS = 420;
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
        serverSocket.setSoTimeout(42069);
        logger.info("Server connected to port: " + port);
        logger.info("Server timeout set: " + serverSocket.getSoTimeout());
    }

    private void waitForConnections() throws IOException
    {
        while (!serverSocket.isClosed())
        {
            try // to accept socket connections
            {
                Socket clientSocket = serverSocket.accept();
                executorService.execute(() -> new ServerRunnable(clientSocket).run());
            }
            catch (IOException ex)
            {
                logger.warning("Error accepting connection: " + ex.getMessage());
            }
        }
    }

    public void stopServer()
    {
        executorService.shutdown();
    }

}

package protohackers.server.d0;

import protohackers.ServerLogFormatter;
import protohackers.Connection;
import protohackers.ServerLogOptions;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server
{
    private static final int PORT = 42069;
    private static final int CLIENTS = 5;
    private static final ServerLogOptions logger = new ServerLogOptions(ServerLogFormatter.getLogger(ServerRunnable.class));

    static void main()
    {
        new Server().start(PORT);
    }

    public void start(int port)
    {
        try (ServerSocket serverSocket = new ServerSocket(port))
        {
            logger.info("New Server connected to port | " + port);
            Executor executor = Executors.newFixedThreadPool(CLIENTS);
            while (!serverSocket.isClosed())
            {
                executor.execute(new ServerRunnable(serverSocket.accept()));
            }
        }
        catch (Exception exception)
        {
            logger.error("Failed to start new Server on port | " + port);
        }
    }
}

class ServerRunnable implements Runnable
{
    Connection client;
    ServerLogOptions logger;

    public ServerRunnable(Socket socket) throws IOException
    {
        this.client = new Connection(socket);
        this.logger = new ServerLogOptions(ServerLogFormatter.getLogger(ServerRunnable.class));
    }

    @Override
    public void run()
    {
        try // to process client connection
        {
            String message;
            while ((message = client.getReader().readLine()) != null)
            {
                logger.info("Received\t | " + message);
                client.getWriter().println(message);
                logger.info("Sent\t\t | " + message);
                client.close();
            }
        }
        catch (IOException e)
        {
            logger.warning("Client disconnected\t   | " + client.getSocket().getInetAddress());
        }
    }
}

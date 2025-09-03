package protohackers.server.d0;

import lombok.extern.slf4j.*;
import protohackers.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

@Slf4j
public class Server
{
    private static final int PORT = 42069;
    private static final int CLIENTS = 5;

    public static void main(String[] args)
    {
        new Server().start(PORT);
    }

    public void start(int port)
    {
        try (ServerSocket serverSocket = new ServerSocket(port))
        {
            log.info("New Server connected to port | {}", port);
            Executor executor = Executors.newFixedThreadPool(CLIENTS);
            while (!serverSocket.isClosed())
            {
                executor.execute(new ServerRunnable(serverSocket.accept()));
            }
        }
        catch (Exception exception)
        {
            log.error("Failed to start new Server on port | {}", port);
        }
    }
}


@Slf4j
class ServerRunnable implements Runnable
{
    Connection client;

    public ServerRunnable(Socket socket) throws IOException
    {
        this.client = new Connection(socket);
    }

    @Override
    public void run()
    {
        try // to process client connection
        {
            String message;
            while ((message = client.getReader().readLine()) != null)
            {
                log.info("Received\t | {}", message);
                client.getWriter().println(message);
                log.info("Sent\t\t | {}", message);
                client.close();
            }
        }
        catch (IOException e)
        {
            log.trace("Client disconnected\t   | {}", client.getSocket().getInetAddress());
        }
    }
}

package protohackers.server.d0;


import lombok.Getter;
import protohackers.ServerLogFormatter;
import protohackers.ServerLogOptions;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server
{
    // private static final String HOST = "localhost";
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
            Executor executor = Executors.newFixedThreadPool(5);
            while (!serverSocket.isClosed())
            {
                executor.execute(new ServerRunnable(serverSocket.accept()));
            }
        }
        catch (Exception exception)
        {
            logger.error("Faile to start new Server on port | " + port);
        }
    }
}

@Getter
class ClientConnection implements Closeable
{
    Socket socket;
    PrintWriter writer;
    BufferedReader reader;
    ServerLogOptions logger;

    public ClientConnection(Socket socket) throws IOException
    {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.logger = new ServerLogOptions(ServerLogFormatter.getLogger(ServerRunnable.class));
    }

    @Override
    public void close()
    {
        try (Writer writer = this.writer;
             Reader reader = this.reader;
             Socket socket = this.socket)
        {
            //  logger.debug(writer.toString());
            //  logger.debug(reader.toString());
            //  logger.debug(socket.toString());
//            logger.debug("Client resources closed. | "
//                    + writer.toString()
//                    + reader.toString()
//                    + socket.toString()
//            );
            logger.debug("Client resources closed | " + socket.getInetAddress());
//            logger.debug("Client resources closed\t\t | " + reader.toString());
//            logger.debug("Client resources closed\t\t | " +  writer.toString());

        }
        catch (IOException e)
        {
            logger.error(e.getMessage());
        }
    }
}


class ServerRunnable implements Runnable
{

    ClientConnection client;
    ServerLogOptions logger;

    public ServerRunnable(Socket socket) throws IOException
    {
        this.client = new ClientConnection(socket);
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
            logger.warning("Client disconnected\t   | " + client.socket.getInetAddress());
        }
    }
}

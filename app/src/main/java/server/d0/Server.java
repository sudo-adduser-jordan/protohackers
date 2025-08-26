package server.d0;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = ServerLogFormatter.getLogger(Server.class);
    private final int PORT = 42069;
    private ServerSocket serverSocket;
    
    public static void main(String[] args) {
        Server server = new Server();
        server.startServer(); 
    }

    public void startServer()
    {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) 
        {
            logger.info("Server started: PORT: " + PORT);

            for (;;) 
            {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected: " + clientSocket.getInetAddress());

                Thread thread = new Thread(new ServerRunnable(clientSocket));
                logger.info("Thread created: " + thread.getName());

                thread.start();
            }
        } 
        catch (IOException e) 
        {
            logger.severe("Server unable to connect: PORT: " + PORT);
            logger.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopServer()
    {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

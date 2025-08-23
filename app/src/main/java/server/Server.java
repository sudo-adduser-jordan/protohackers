package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    
    public static void main(String[] args) {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new ServerLogFormatter());
        logger.addHandler(handler);
        logger.setUseParentHandlers(false); 

        final int PORT = 42069;

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
}

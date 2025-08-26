package server;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Server {
    private static final Logger logger = ServerLogFormatter.getLogger(Server.class);
    
    public static void main(String[] args) {
        final int PORT = 42069;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) 
        {
            logger.info("Server started: PORT: " + PORT);

            for (;;) 
            {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected: " + clientSocket.getInetAddress());
          
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                objectMapper.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
                logger.info("JSON mapper created for client: " + clientSocket.getInetAddress());

                Thread thread = new Thread(new ServerRunnable(clientSocket, objectMapper));
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

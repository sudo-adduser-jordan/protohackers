package server.d0;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

public class ServerRunnable implements Runnable {
    private static final Logger logger = Logger.getLogger(ServerRunnable.class.getName());
    private Socket socket;

    public ServerRunnable(Socket socket) { this.socket = socket; };

    public void run() {
        try (
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
        ) {
            int inputByte;
            while ((inputByte = input.read()) != -1) {
                output.write(inputByte);
            }
        } 
        catch (IOException e) 
        {
            logger.warning("Connection error with client: " + socket.getInetAddress());
            e.printStackTrace();
        } 
    }
}
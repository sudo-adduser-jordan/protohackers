package server;

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
            // BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
        ) {
            int inputByte;
            while ((inputByte = input.read()) != -1) {
                // logger.info("Received from client: " + inputByte);
                output.write(inputByte);
                // logger.info("Sent to client: " + inputByte);
            }
            // String message;
            // while ((message = in.readLine()) != null) {
            //     logger.info("Received from client: " + message);
            //     out.println(message);
            //     logger.info("Sent to client: " + message);
            // }
        } 
        catch (IOException e) 
        {
            logger.warning("Connection error with client: " + socket.getInetAddress());
            e.printStackTrace();
        } 
        finally 
        {
            try { socket.close(); } 
            catch (IOException e) { e.printStackTrace();}
            logger.info("Client disconnected: " + socket.getInetAddress());
        }
    }
}
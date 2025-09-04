package protohackers.client;

import lombok.extern.slf4j.*;

import java.io.*;
import java.net.*;

@Slf4j
public class Client
{
    public static void main(String[] args)
    {
        String host = "localhost";
        int port = 42069;

        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())))
        {
            String message = "echo";
            writer.write(message);
            writer.flush();

            String responseLine = reader.readLine();
            log.info("Response: {}", responseLine);

            String[] messages = {"echo", "echo", "echo", "echo", "echo",};

            for (String request : messages)
            {
                log.info("Sending: {}", request.trim());
                writer.write(request);
                writer.flush();

                String response = reader.readLine();
                log.info("Received: {}", response);
            }
        }
        catch (IOException e)
        {
            log.error("Socket unable to connect to host: {}", host);
            log.error("Socket unable to connect to port: {}", port);
        }
    }
}
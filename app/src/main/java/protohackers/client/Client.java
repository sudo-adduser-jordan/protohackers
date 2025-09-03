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

            String request = "echo"; // test with a prime number
            writer.write(request);
            writer.flush();

            String responseLine = reader.readLine();
            log.info("Response: " + responseLine);

            // You can send multiple requests:
            String[] testRequests = {"echo", "echo", "echo", "echo", "echo",};

            for (String req : testRequests)
            {
                log.info("Sending: " + req.trim());
                writer.write(req);
                writer.flush();

                String resp = reader.readLine();
                log.info("Received: " + resp);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
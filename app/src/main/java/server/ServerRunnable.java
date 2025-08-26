package server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ServerRunnable implements Runnable {
    private static final Logger logger = ServerLogFormatter.getLogger(Server.class);
    private Socket socket;
    private ObjectMapper objectMapper;

    public ServerRunnable(Socket socket, ObjectMapper objectMapper) 
    { 
        this.socket = socket;
        this.objectMapper = objectMapper;
    };

    public void run() {
        try 
        {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
            BufferedOutputStream output = new BufferedOutputStream(outputStream);

            while(!socket.isClosed()) {
            
            String response = input.readLine();
            if (response == null ) { 
                logger.warning("Request: " + null);
                logger.severe("Client disconnected: " + socket.getInetAddress());
                socket.close();
            } // check null
            logger.warning("Request: " + response);

            try {
                RequestJSON requestJSON = objectMapper.readValue(response, RequestJSON.class);
                boolean isPrime = isPrimeByBigInteger(requestJSON.getNumber());

                ResponseJSON responseJSON = new ResponseJSON("isPrime", isPrime);
                logger.warning("Response: " + objectMapper.writeValueAsString(responseJSON));
                
                output.write(objectMapper.writeValueAsString(responseJSON).getBytes());
                output.flush();

            }  catch (Exception e) {
                logger.warning("Invalid JSON: " + e.toString());
                logger.warning("Response: " + response);
                
                output.write(response.getBytes());
                output.flush();
                socket.close();
                logger.severe("Client disconnected: " + socket.getInetAddress());
            }
            logger.info("Response sent to : " + socket.getInetAddress());
        }
        } 
        catch (IOException e) 
        {
            logger.warning("Connection error with client: " + socket.getInetAddress());
            e.printStackTrace();
        } 
    }


    // private void handleClient() {};

    private void echoResponse(InputStream input, OutputStream output) throws IOException
    {
        int inputByte;
        while ((inputByte = input.read()) != -1) {
            output.write(inputByte);
        }
        output.flush();
    };

    private boolean isPrimeByBigInteger(int number) 
    {
        BigInteger bigInt = BigInteger.valueOf(number);
        return bigInt.isProbablePrime(100); // effecient handling of small and large primes
    }
}
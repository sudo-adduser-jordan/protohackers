package server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ServerRunnable implements Runnable {
    private static final Logger logger = Logger.getLogger(ServerRunnable.class.getName());
    private Socket socket;
    private ObjectMapper objectMapper;

    public ServerRunnable(Socket socket, ObjectMapper objectMapper) 
    { 
        this.socket = socket;
        this.objectMapper = objectMapper;
    };

    public void run() {
        try (
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
            BufferedOutputStream output = new BufferedOutputStream(outputStream);

        ) {
            try {
                RequestJSON requestJSON = objectMapper.readValue(input, RequestJSON.class);
                logger.info(requestJSON.toString());

                boolean isPrime = isPrimeByBigInteger(requestJSON.getNumber());
                ResponseJSON responseJSON = new ResponseJSON("isPrime", isPrime);
                String response = objectMapper.writeValueAsString(responseJSON);
                output.write((response + "\n").getBytes());
                output.flush();

            } catch (Exception e) {
                echoResponse(inputStream, outputStream);
                socket.close();
                return;
            };
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

    private void echoResponse(InputStream input, OutputStream output) throws IOException
    {
        int inputByte;
        while ((inputByte = input.read()) != -1) {
            output.write(inputByte);
        }
    };

    private boolean isPrimeByBigInteger(int number) 
    {
        BigInteger bigInt = BigInteger.valueOf(number);
        return bigInt.isProbablePrime(100); // effecient handling of small and large primes
    }
}
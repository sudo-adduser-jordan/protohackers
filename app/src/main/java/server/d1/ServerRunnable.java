package server.d1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import server.ServerLogFormatter;

public class ServerRunnable implements Runnable
{
    private static final Logger logger = ServerLogFormatter.getLogger(Server.class);
    private Socket socket;
    private JsonMapper jsonMapper;

    public ServerRunnable(Socket socket)
    {
        this.socket = socket;
        this.jsonMapper = JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                .build();
    };

    public void run()
    {
        try // to initialize streams
        {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(outputStream));

            while (!socket.isClosed())
            { // accept requests

                String request = input.readLine();
                if (request == null)
                {
                    logger.warning("Request: " + null);
                    logger.severe("Client disconnected: " + socket.getInetAddress());
                    socket.close();
                }
                logger.warning("Request: " + request);

                try // to process request
                {
                    RequestJSON requestJSON = jsonMapper.readValue(request, RequestJSON.class);
                    if (!Objects.equals(requestJSON.getMethod(), "isPrime"))
                    {
                        throw new Exception("method does not equal 'isPrime'");
                    }

                    boolean isPrime = isPrimeDouble(requestJSON.getNumber());

                    ResponseJSON responseJSON = new ResponseJSON("isPrime", isPrime);

                    output.write(jsonMapper.writeValueAsString(responseJSON));
                    output.newLine();
                    output.flush();

                    logger.warning("Response: " + jsonMapper.writeValueAsString(responseJSON));

                }
                catch (Exception e)
                {
                    logger.warning("Invalid JSON: " + e.toString());
                    logger.warning("Response: " + request);

                    if (request != null)
                    {

                        output.write(request);
                        output.newLine();
                        output.flush();
                    }

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

    private boolean isPrimeDouble(double number)
    {
        if (number != Math.floor(number) || number <= 1)
            return false;
        BigInteger bigInt = BigInteger.valueOf((long) number);
        return bigInt.isProbablePrime(100);
    }
}
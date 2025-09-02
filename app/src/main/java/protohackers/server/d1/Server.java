package protohackers.server.d1;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.*;
import lombok.*;
import protohackers.*;

import java.io.*;
import java.math.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

@Data // {"method":"isPrime","prime":false}
@AllArgsConstructor
class ResponseJSON
{
    private String method;
    private boolean isPrime;
}

@Data // {"method":"isPrime","number":123}
@JsonPropertyOrder({"method", "number"})
class RequestJSON
{
    private String method;
    private double number;

    public RequestJSON(
            @JsonProperty(value = "method", required = true)
            String method,
            @JsonProperty(value = "number", required = true)
            double number)
    {
        this.method = method;
        this.number = number;
    }
}

public class Server
{
    private static final int PORT = 42069;
    private static final int CLIENTS = 5;
    private static final ServerLogOptions logger = new ServerLogOptions(ServerLogFormatter.getLogger(ServerRunnable.class));

    static void main()
    {
        new Server().start(PORT);
    }

    public static boolean isPrime(double number)
    {
        if (number != Math.floor(number) || number <= 1) return false;
        BigInteger bigInt = BigInteger.valueOf((long) number);
        return bigInt.isProbablePrime(100);
    }

    public void start(int port)
    {
        try (ServerSocket serverSocket = new ServerSocket(port))
        {
            logger.info("New Server connected to port | " + port);
            Executor executor = Executors.newFixedThreadPool(CLIENTS);
            while (!serverSocket.isClosed())
            {
                executor.execute(new ServerRunnable(serverSocket.accept()));
            }
        }
        catch (Exception exception)
        {
            logger.error("Failed to start new Server on port | " + port);
        }
    }
}

class ServerRunnable implements Runnable
{
    Connection client;
    ServerLogOptions logger;

    public ServerRunnable(Socket socket) throws IOException
    {
        this.client = new Connection(socket);
        this.logger = new ServerLogOptions(ServerLogFormatter.getLogger(ServerRunnable.class));
    }

    private String processMessage(String requestString, JsonMapper jsonMapper)
    {
        try // to process message
        {
            RequestJSON requestJSON = jsonMapper.readValue(requestString, RequestJSON.class);
            if (!Objects.equals(requestJSON.getMethod(), "isPrime"))
            {
                return null;
            }
            ResponseJSON responseJSON = new ResponseJSON("isPrime", Server.isPrime(requestJSON.getNumber()));

            return jsonMapper.writeValueAsString(responseJSON);
        }
        catch (JsonProcessingException e)
        {
            logger.debug("JSON processing exception");
            return null;
        }
    }

    @Override
    public void run()
    {
        try // to process client connection
        {
            String message;
            while ((message = client.getReader().readLine()) != null)
            {
                logger.info("Received\t | " + message);

                JsonMapper jsonMapper = JsonMapper.builder()
                                                  .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                                                  .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                                                  .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                                                  .build();

                String response = processMessage(message, jsonMapper);
                if (null == response)
                {
                    client.getWriter().println(message);
                    logger.info("Sent\t\t | " + message);
                    client.getSocket().close();
                    client.close();
                }
                else
                {
                    client.getWriter().println(response);
                    logger.info("Sent\t\t | " + response);
                }
            }
        }
        catch (IOException e)
        {
            logger.warning("Client disconnected\t   | " + client.getSocket().getInetAddress());
        }
    }
}


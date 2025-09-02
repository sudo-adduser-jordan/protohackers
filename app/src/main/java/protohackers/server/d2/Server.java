package protohackers.server.d2;

import lombok.*;
import protohackers.*;

import java.io.*;
import java.math.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

enum MessageTypes
{
    INSERT, QUERY
}

// Byte:  |  0  |  1     2     3     4  |  5     6     7     8  |
// Type:  |char |         int32         |         int32         |
@Getter
@Builder
class Request
{
    private final MessageTypes MessageType;
    private final int FirstValue; // 32
    private final int SecondValue; // 32

    @Override // review format
    public String toString()
    {
        return "%s, %d, %d".formatted(MessageType.toString(), FirstValue, SecondValue);
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

    public static boolean sumAverage(double number)
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

    private String processMessage(String message)
    {
        try
        {
            return null;
        }
        catch (Exception e)
        {
            logger.debug("Message processing exception");
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
                client.getWriter().println(message);
                logger.info("Sent\t\t | " + message);
            }
        }
        catch (IOException e)
        {
            logger.warning("Client disconnected\t   | " + client.getSocket().getInetAddress());
        }
    }
}

class SessionMemoryCache
{
    private final Map<Integer, Integer> treeMap; // Using TreeMap to keep timestamps ordered

    public SessionMemoryCache()
    {
        this.treeMap = new TreeMap<>();
    }

    public void addPrice(int timestamp, int price)
    {
        this.treeMap.put(timestamp, price);
    }

    public Integer getPrice(int timestamp)
    {
        return this.treeMap.get(timestamp);
    }

    // mintime <= T <= maxtime
    public Integer getAveragePriceInRange(int minTimestamp, int maxTimestamp)
    {
        Set<Integer> validTimestamps = treeMap.keySet()
                                              .stream()
                                              .filter(t -> minTimestamp <= t && t <= maxTimestamp)
                                              .collect(Collectors.toSet());

        if (validTimestamps.isEmpty()) return 0;

        long sum = validTimestamps.stream().mapToLong(treeMap::get).sum();

        return (int) (sum / validTimestamps.size());
    }
}


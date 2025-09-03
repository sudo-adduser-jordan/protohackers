package protohackers.server.d2;

import com.sun.net.httpserver.*;
import lombok.*;
import protohackers.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class Server
{
    private static final ServerLogOptions logger = new ServerLogOptions(ServerLogFormatter.getLogger(ServerRunnable.class));
    private static final int PORT = 42069;
    private static final int CLIENTS = 5;

    static void main()
    {
        new Server().start(PORT);
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
    SessionMemoryCache sessionMemoryCache;

    public final int REQUEST_LENGTH = 9; // char, 2 ints
    public final int RESPONSE_LENGTH = 4; // 1 int

    public ServerRunnable(Socket socket) throws IOException
    {
        this.client = new Connection(socket);
        this.sessionMemoryCache = new SessionMemoryCache();
        this.logger = new ServerLogOptions(ServerLogFormatter.getLogger(ServerRunnable.class));
    }


    // Byte:  |  0  |  1     2     3     4  |  5     6     7     8  |
    // Type:  |char |         int32         |         int32         |
    private String processMessage(String message)
    {
        if (null == message) return null;

        if (REQUEST_LENGTH != message.length()) return null;

        if ('I' == message.charAt(0))
        { // insert
            int timestamp = Integer.parseInt(message.substring(1, 4));
            int price = Integer.parseInt(message.substring(5, 8));
            sessionMemoryCache.addPrice(timestamp, price);
            return null;
        }
        if ('Q' == message.charAt(0))
        { // query
            int mintime = Integer.parseInt(message.substring(1, 4));
            int maxtime = Integer.parseInt(message.substring(5, 8));
            return sessionMemoryCache.getAveragePriceInRange(mintime, maxtime).toString();
        }
        return null;
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
                String response = processMessage(message);
                if (null == response)
                {
                    client.getWriter().println("420");
                    client.close();
                }
                else
                {
                    client.getWriter().println(response);
                }
                logger.info("Sent\t\t | " + response);
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


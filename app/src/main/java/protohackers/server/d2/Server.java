package protohackers.server.d2;

import lombok.extern.slf4j.*;
import protohackers.*;

import java.io.*;
import java.math.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

@Slf4j
public class Server
{
    private static final int PORT = 42069;
    private static final int CLIENTS = 5;

    public static void main(String[] args)
    {
        new Server().start(PORT);
    }

    public void start(int port)
    {
        try (ServerSocket serverSocket = new ServerSocket(port))
        {
            log.info("New Server connected to port | {}", port);
            Executor executor = Executors.newFixedThreadPool(CLIENTS);
            while (!serverSocket.isClosed())
            {
                executor.execute(new ServerRunnable(serverSocket.accept()));
            }
        }
        catch (Exception exception)
        {
            log.error("Failed to start new Server on port | {}", port);
        }
    }
}

@Slf4j
class ServerRunnable implements Runnable
{
    Connection client;
    SessionMemoryCache sessionMemoryCache;

    public final int REQUEST_LENGTH = 9; // char, 2 ints
    //    public final int RESPONSE_LENGTH = 4; // 1 int

    public ServerRunnable(Socket socket) throws IOException
    {
        this.client = new Connection(socket);
        this.sessionMemoryCache = new SessionMemoryCache();
    }



    @Override
    public void run()
    {
        try // to process client connection
        {
            while (!client.getSocket().isClosed())
            {
                ByteBuffer messageByteBuffer = ByteBuffer.wrap(client.getSocket().getInputStream().readNBytes(9));
                byte[] messageBytes = messageByteBuffer.array(); // this will clear out the buffer
                String message = null;

                if (messageBytes.length != REQUEST_LENGTH)
                {
                    client.close();
                    return;
                }
                // Byte:  |  0  |  1     2     3     4  |  5     6     7     8  |
                // Type:  |char |         int32         |         int32         |
                int firstValue = ByteBuffer.wrap(messageBytes, 1, 4).order(ByteOrder.BIG_ENDIAN).getInt();
                int secondValue = ByteBuffer.wrap(messageBytes, 5, 4).order(ByteOrder.BIG_ENDIAN).getInt();
                if ('I' == messageBytes[0])
                { // insert
                    message = 'I' + Integer.toString(firstValue) + Integer.toString(secondValue);
                    sessionMemoryCache.addPrice(firstValue, secondValue);
                }
                int average= 0;
                if ('Q' == messageBytes[0])
                { // query
                    message = 'Q' + Integer.toString(firstValue) + Integer.toString(secondValue);
                    average = sessionMemoryCache.getAveragePriceInRange(firstValue, secondValue);
                    client.getSocket().getOutputStream().write(ByteBuffer.allocate(4).putInt(average).array());
                    client.getSocket().getOutputStream().flush();
                    client.close();
                }

                log.info("Received string  | {}", message);
                log.info("Received bytes   | {}", messageBytes);
                log.info("Sent bytes       | {}", ByteBuffer.allocate(4).putInt(average).array());
            }
        }
        catch (IOException e)
        {
            log.trace("Client disconnected\t   | {}", client.getSocket().getInetAddress());
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


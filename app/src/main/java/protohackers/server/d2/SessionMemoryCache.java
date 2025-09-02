package protohackers.server.d2;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SessionMemoryCache
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

        long sum = validTimestamps.stream()
                                  .mapToLong(treeMap::get)
                                  .sum();

        return (int) (sum / validTimestamps.size());
    }
}

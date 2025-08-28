package server.d2;

import java.util.Map;
import java.util.TreeMap;

public class SessionMemoryCache
{
	private Map<Integer, Integer> treeMap; // Using TreeMap to keep timestamps ordered

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
	public Double getAveragePriceInRange(int mintime, int maxtime)
	{
		if (maxtime < mintime) return null;

		int sum = 0;
		int count = 0;

		for (Map.Entry<Integer, Integer> entry : this.treeMap.entrySet())
		{
			int timestamp = entry.getKey();
			if (mintime <= timestamp && timestamp <= maxtime)
			{
				sum += entry.getValue();
				count++;
			}
			if (timestamp > maxtime) break;
		} 
		if (count == 0) return null;

		return (double) sum / count;
	}
}

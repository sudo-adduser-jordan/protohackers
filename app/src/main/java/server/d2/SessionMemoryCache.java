package server.d2;

import java.util.Map;
import java.util.TreeMap;

public class SessionMemoryCache
{
	private Map<Long, Integer> treeMap; // Using TreeMap to keep timestamps ordered if needed

	public SessionMemoryCache()
	{
		this.treeMap = new TreeMap<>();
	}

	public void addPrice(long timestamp, int price)
	{
		this.treeMap.put(timestamp, price);
	}

	public Integer getPrice(long timestamp)
	{
		return this.treeMap.get(timestamp);
	}

	public Map<Long, Integer> getAllData()
	{
		return this.treeMap;
	}
}

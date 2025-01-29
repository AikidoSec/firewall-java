package dev.aikido.agent_api.ratelimiting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public class LRUCache<K, V> {
    private final int maxItems;
    private final long timeToLiveInMs;
    private final Map<K, CacheEntry<V>> cache;

    public LRUCache(int maxItems, long timeToLiveInMs) {
        this.maxItems = maxItems;
        this.timeToLiveInMs = timeToLiveInMs;
        this.cache = new LinkedHashMap<K, CacheEntry<V>>(maxItems, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                return size() > maxItems;
            }
        };
    }

    public V get(K key) {
        if (cache.containsKey(key)) {
            CacheEntry<V> entry = cache.get(key);
            // Check if the item is still valid based on TTL
            if (getUnixTimeMS() - entry.startTime < timeToLiveInMs) {
                return entry.value; // Return the actual value
            } else {
                // Remove expired item
                cache.remove(key);
            }
        }
        return null;
    }

    public void set(K key, V value) {
        if (cache.containsKey(key)) {
            cache.remove(key); // Remove the existing item
        } else if (cache.size() >= maxItems) {
            // Remove the oldest item (the eldest entry)
            K eldestKey = cache.keySet().iterator().next();
            cache.remove(eldestKey);
        }
        cache.put(key, new CacheEntry<V>(value, getUnixTimeMS())); // Store value and timestamp
    }

    public void clear() {
        cache.clear();
    }

    public void delete(K key) {
        cache.remove(key);
    }

    public List<K> keys() {
        return cache.keySet().stream().toList();
    }

    public int size() {
        return cache.size();
    }

    private static class CacheEntry<V> {
        V value;
        long startTime;

        CacheEntry(V value, long startTime) {
            this.value = value;
            this.startTime = startTime;
        }
    }
}

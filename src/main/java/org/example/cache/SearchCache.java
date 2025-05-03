package org.example.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class SearchCache {
    private static final int MAX_SIZE = 100;
    private static final long TTL = 10L * 60 * 1000;
    private final Map<String, Object> cache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_SIZE;
        }
    };

    public SearchCache() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::clearExpired, TTL, TTL, TimeUnit.MILLISECONDS);
    }

    public synchronized void put(String key, Object value) {
        cache.put(key, new CacheEntry(value));
    }

    public synchronized Object get(String key) {
        CacheEntry entry = (CacheEntry) cache.get(key);
        return entry == null ? null : entry.value;
    }

    public synchronized boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    private synchronized void clearExpired() {
        cache.entrySet().removeIf(entry ->
                ((CacheEntry) entry.getValue()).isExpired()
        );
    }

    public synchronized void remove(String key) {
        cache.remove(key);
    }

    public synchronized void clear() {
        cache.clear();
    }

    private static class CacheEntry {
        final Object value;
        final long timestamp;

        CacheEntry(Object value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > TTL;
        }
    }
}
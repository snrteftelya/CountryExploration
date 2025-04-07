package org.example.cache;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private final Map<String, CacheEntry> cache;
    private final long defaultTtlMillis;
    private final int maxSize;

    public CacheService() {
        this(1000, 30, TimeUnit.MINUTES); // Default values
    }

    public CacheService(int maxSize, long ttl, TimeUnit timeUnit) {
        this.cache = new ConcurrentHashMap<>(maxSize);
        this.maxSize = maxSize;
        this.defaultTtlMillis = timeUnit.toMillis(ttl);
        logger.info("Cache initialized with maxSize={} and TTL={} ms", maxSize, defaultTtlMillis);
    }

    public boolean containsKey(String key) {
        CacheEntry entry = cache.get(key);
        return entry != null && !entry.isExpired();
    }

    public void remove(String key) {
        cache.remove(key);
        logger.debug("Removed cache entry for key: {}", key);
    }

    public void put(String key, Object value) {
        put(key, value, defaultTtlMillis);
    }

    public synchronized void put(String key, Object value, long ttlMillis) {
        cleanExpired();

        if (cache.size() >= maxSize) {
            evictLruEntry();
        }

        cache.put(key, new CacheEntry(value, System.currentTimeMillis() + ttlMillis));
        logger.debug("Cached value for key: {}", key);
    }

    public Object get(String key) {
        CacheEntry entry = cache.get(key);

        if (entry == null) {
            logger.debug("Cache miss for key: {}", key);
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            logger.debug("Expired cache entry removed for key: {}", key);
            return null;
        }

        entry.updateLastAccess();
        logger.debug("Cache hit for key: {}", key);
        return entry.getValue();
    }

    private synchronized void cleanExpired() {
        int initialSize = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        if (cache.size() != initialSize) {
            logger.debug("Cleaned {} expired entries", initialSize - cache.size());
        }
    }

    private void evictLruEntry() {
        String lruKey = cache.entrySet().stream()
                .min(Comparator.comparingLong(e -> e.getValue().getLastAccessTime()))
                .map(Map.Entry::getKey)
                .orElse(null);

        if (lruKey != null) {
            cache.remove(lruKey);
            logger.debug("Evicted LRU entry with key: {}", lruKey);
        }
    }

    private static class CacheEntry {
        private final Object value;
        private final long expirationTime;
        private volatile long lastAccessTime;

        CacheEntry(Object value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
            this.lastAccessTime = System.currentTimeMillis();
        }

        Object getValue() {
            return value;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }

        long getLastAccessTime() {
            return lastAccessTime;
        }

        void updateLastAccess() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
}

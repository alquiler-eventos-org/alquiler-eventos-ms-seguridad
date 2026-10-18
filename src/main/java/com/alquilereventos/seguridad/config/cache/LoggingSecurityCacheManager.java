package com.alquilereventos.seguridad.config.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/** Registra hit/miss/set/delete sin registrar valores sensibles. */
final class LoggingSecurityCacheManager implements CacheManager {
    private final CacheManager delegate;
    private final Map<String, Cache> caches = new ConcurrentHashMap<>();
    LoggingSecurityCacheManager(CacheManager delegate) { this.delegate = delegate; }
    public Cache getCache(String name) {
        Cache cache = delegate.getCache(name);
        return cache == null ? null : caches.computeIfAbsent(name, ignored -> new LoggedCache(cache));
    }
    public Collection<String> getCacheNames() { return delegate.getCacheNames(); }

    private static final class LoggedCache implements Cache {
        private static final Logger log = LoggerFactory.getLogger(LoggedCache.class);
        private final Cache delegate;
        LoggedCache(Cache delegate) { this.delegate = delegate; }
        public String getName() { return delegate.getName(); }
        public Object getNativeCache() { return delegate.getNativeCache(); }
        public ValueWrapper get(Object key) {
            try {
                ValueWrapper result = delegate.get(key);
                log.info("CACHE GET cache={} key={} result={}", getName(), key, result == null ? "MISS" : "HIT");
                return result;
            } catch (RuntimeException ex) { log.warn("CACHE ERROR operation=GET cache={}; continua sin cache", getName(), ex); return null; }
        }
        public <T> T get(Object key, Class<T> type) {
            try { T result = delegate.get(key, type); log.info("CACHE GET cache={} key={} result={}", getName(), key, result == null ? "MISS" : "HIT"); return result; }
            catch (RuntimeException ex) { log.warn("CACHE ERROR operation=GET cache={}; continua sin cache", getName(), ex); return null; }
        }
        public <T> T get(Object key, Callable<T> loader) { return delegate.get(key, loader); }
        public void put(Object key, Object value) {
            try { delegate.put(key, value); log.info("CACHE SET cache={} key={}", getName(), key); }
            catch (RuntimeException ex) { log.warn("CACHE ERROR operation=SET cache={}; continua sin cache", getName(), ex); }
        }
        public ValueWrapper putIfAbsent(Object key, Object value) { return delegate.putIfAbsent(key, value); }
        public void evict(Object key) {
            try { delegate.evict(key); log.info("CACHE DELETE cache={} key={}", getName(), key); }
            catch (RuntimeException ex) { log.warn("CACHE ERROR operation=DELETE cache={}; continua sin cache", getName(), ex); }
        }
        public boolean evictIfPresent(Object key) { return delegate.evictIfPresent(key); }
        public void clear() { delegate.clear(); }
        public boolean invalidate() { return delegate.invalidate(); }
    }
}

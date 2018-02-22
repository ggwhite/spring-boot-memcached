package lab.ggwhite.spring.boot.support.memcached.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import lab.ggwhite.spring.boot.support.memcached.cache.MemcachedCacheProperties.CacheConfig;
import net.spy.memcached.MemcachedClient;

public class MemcachedCacheManager implements CacheManager, DisposableBean {

	private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>();
	
	private final ConcurrentMap<String, CacheConfig> cacheConfigMap = new ConcurrentHashMap<String, CacheConfig>();

	final MemcachedClient memcachedClient;
	
	final MemcachedCacheProperties properties;
	
	public MemcachedCacheManager(MemcachedClient memcachedClient, MemcachedCacheProperties properties) {
    	this.memcachedClient = memcachedClient;
    	this.properties = properties;
    	
    	for (CacheConfig cfg : properties.getCaches()) {
    		this.cacheConfigMap.putIfAbsent(cfg.getName(), cfg);
    	}
    }

	public void destroy() throws Exception {
		this.memcachedClient.shutdown();
	}

	public Cache getCache(String name) {
		Cache cache = this.cacheMap.get(name);
		if (cache == null) {
			cache = new MemcachedCache(memcachedClient, name, expiration(name), properties.getPrefix());
			final Cache currentCache = cacheMap.putIfAbsent(name, cache);
			if (currentCache != null) {
                cache = currentCache;
            }
		}
		return cache;
	}

	public Collection<String> getCacheNames() {
		return Collections.unmodifiableSet(cacheMap.keySet());
	}
	
	private int expiration(String name) {
		if (cacheConfigMap.containsKey(name)) {
			return cacheConfigMap.get(name).getExpiration();
		}
		return MemcachedCacheDefault.EXPIRATION;
	}

}

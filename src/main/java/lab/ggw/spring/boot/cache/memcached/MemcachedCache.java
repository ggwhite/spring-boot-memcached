package lab.ggw.spring.boot.cache.memcached;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.cache.support.AbstractValueAdaptingCache;

import net.spy.memcached.MemcachedClient;

public class MemcachedCache extends AbstractValueAdaptingCache {
	
	private final String KEY_SEPARATOR = MemcachedCacheDefault.KEY_SEPARATOR;
	private final String NAMESPACE = MemcachedCacheDefault.NAMESPACE;
	
	private final MemcachedClient memcachedClient;
	
	private final String cacheName;
	private final int expiration;
	private final String keyPrefix;
	private final String namespaceKey;
	
	private final Lock lock = new ReentrantLock();

	public MemcachedCache(MemcachedClient memcachedClient, String cacheName, int expiration, String prefix) {
		super(true);
		this.memcachedClient = memcachedClient;
		this.cacheName = cacheName;
		this.expiration = expiration;
		
		StringBuilder sb = new StringBuilder(prefix)
				.append(KEY_SEPARATOR)
				.append(cacheName)
				.append(KEY_SEPARATOR);
		this.keyPrefix = sb.toString();
		this.namespaceKey = sb.append(NAMESPACE).toString();
	}

	@Override
	protected Object lookup(Object key) {
		return memcachedClient.get(memcachedKey(key));
	}
	
	public String getName() {
		return this.cacheName;
	}

	public Object getNativeCache() {
		return this.memcachedClient;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Callable<T> valueLoader) {
		Object value = lookup(key);
		
		if (value != null) {
			return (T) fromStoreValue(value);
		}
		
		lock.lock();
		
		try {
			return (T) fromStoreValue(loadValue(key, valueLoader));
		} finally {
			lock.unlock();
		}
	}
	
	public void put(Object key, Object value) {
		this.memcachedClient.set(memcachedKey(key), this.expiration, toStoreValue(value));
	}

	public ValueWrapper putIfAbsent(Object key, Object value) {
		Object existingValue = lookup(key);
		if (existingValue == null) {
			put(key, value);
			return toValueWrapper(value);
		}
		return toValueWrapper(existingValue);
	}

	public void evict(Object key) {
		this.memcachedClient.delete(memcachedKey(key));
	}

	public void clear() {
		this.memcachedClient.incr(this.namespaceKey, 1);
	}
	
	private String memcachedKey(Object key) {
		return new StringBuilder()
				.append(this.keyPrefix)
				.append(this.namespaceValue())
				.append(this.KEY_SEPARATOR)
				.append(String.valueOf(key).replaceAll("\\s", ""))
				.toString();
	}
	
	private <T> T loadValue(Object key, Callable<T> valueLoader) {
		T value;
		try {
			value = valueLoader.call();
		} catch (Exception e) {
			throw new ValueRetrievalException(key, valueLoader, e);
		}
		put(key, value);
		return value;
	}

	private String namespaceValue() {
		String value = (String) this.memcachedClient.get(this.namespaceKey);
		if (value == null) {
			value = String.valueOf(System.currentTimeMillis());
			this.memcachedClient.set(this.namespaceKey, this.expiration, value);
		}
		return value;
	}
}

package lab.ggw.spring.boot.cache.memcached;

import java.util.Collections;
import java.util.List;

import lab.ggw.spring.boot.cache.memcached.MemcachedCacheProperties.Server;

public final class MemcachedCacheDefault {

	public static final String HOSTNAME = "localhost";
	
	public static final int PORT = 11211;

	public static final List<Server> SERVERS = Collections.unmodifiableList(Collections.singletonList(new Server(HOSTNAME, PORT)));

	public static final int EXPIRATION = 60;

	public static final String PREFIX = "lab-ggwhite-memcached";
	
	public static final String KEY_SEPARATOR = ":";
	
	public static final String NAMESPACE = "NAMESPACE";

	private MemcachedCacheDefault() {
		throw new AssertionError("Suppress default constructor");
	}
}

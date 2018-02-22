package lab.ggwhite.spring.boot.support.memcached.cache;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@ConfigurationProperties(prefix = "spring.memcached")
public class MemcachedCacheProperties {

	private List<Server> servers = MemcachedCacheDefault.SERVERS;
	
	private String prefix = MemcachedCacheDefault.PREFIX;
	
	private List<CacheConfig> caches = new ArrayList<CacheConfig>();
	
	@Data
	@AllArgsConstructor
	public static class Server {
		private String hostname = MemcachedCacheDefault.HOSTNAME;
		private int port = MemcachedCacheDefault.PORT;
	}
	
	@Data
	public static class CacheConfig {
		private String name;
		private int expiration = MemcachedCacheDefault.EXPIRATION;
	}
}

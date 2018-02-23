package lab.ggw.spring.boot.cache.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lab.ggw.spring.boot.cache.memcached.MemcachedCacheProperties.Server;
import net.spy.memcached.MemcachedClient;

@Configuration
@ConditionalOnClass({ MemcachedClient.class, CacheManager.class })
@ConditionalOnBean(CacheAspectSupport.class)
@ConditionalOnMissingBean({ CacheManager.class, CacheResolver.class })
@EnableConfigurationProperties(MemcachedCacheProperties.class)
@AutoConfigureBefore(CacheAutoConfiguration.class)
public class MemcachedCacheAutoConfiguration {

	private final MemcachedCacheProperties properties;

    @Autowired
    public MemcachedCacheAutoConfiguration(MemcachedCacheProperties cacheProperties) {
        this.properties = cacheProperties;
    }
	
    private MemcachedClient memcachedClient() throws IOException {
    	final List<Server> servers = properties.getServers();
    	
    	List<InetSocketAddress> addrs = new ArrayList<InetSocketAddress>();
    	
    	for (Server server : servers) {
    		addrs.add(new InetSocketAddress(server.getHostname(), server.getPort()));
    	}
    	
    	return new MemcachedClient(addrs);
    }
    
    @Bean
    public MemcachedCacheManager cacheManager() throws IOException {
    	MemcachedCacheManager cacheManager = new MemcachedCacheManager(memcachedClient(), properties);
    	return cacheManager;
    }
    
}

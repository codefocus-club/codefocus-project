package club.codefocus.framework.cache.cacheable;

import club.codefocus.framework.cache.properties.CodeFocusRedisProperties;
import club.codefocus.framework.cache.handler.RedisHandler;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: jackl
 * @Date: 2019/11/13 13:10
 * @Description:
 */
@Slf4j
public class RedisCaffeineCacheManager implements CacheManager {

	private ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>();

	private CodeFocusRedisProperties codeFocusRedisProperties;


	private boolean dynamic = true;

	RedisHandler redisHandler;


	public RedisCaffeineCacheManager(CodeFocusRedisProperties codeFocusRedisProperties, RedisHandler redisHandler
									 ) {
		super();
		this.codeFocusRedisProperties = codeFocusRedisProperties;
		this.dynamic = codeFocusRedisProperties.getCacheConfig().isDynamic();
		this.redisHandler=redisHandler;
	}


	@Override
	public Cache getCache(String name) {
		long expiration=0;
		if(name.contains("#")){
			String[] split = name.split("#");
			if(split.length>1){
				name=split[0];
				expiration=Long.valueOf(split[1]);
			}
		}
		Cache cache = cacheMap.get(name);
		if(cache != null) {
			return cache;
		}
		cache = new RedisCaffeineCache(name,
				caffeineCache(name,expiration), codeFocusRedisProperties,expiration,redisHandler);
		Cache oldCache = cacheMap.putIfAbsent(name, cache);
		return oldCache == null ? cache : oldCache;
	}

	@Override
	public Collection<String> getCacheNames() {
		return null;
	}

	public CaffeineCache caffeineCache(String name, long expiration){
		Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
		if(expiration > 0) {
			cacheBuilder.expireAfterWrite(expiration, TimeUnit.SECONDS);
		}
		int initialCapacity = codeFocusRedisProperties.getCacheConfig().getCaffeine().getInitialCapacity();
		if(initialCapacity>0){
			cacheBuilder.initialCapacity(initialCapacity);
		}
		long maximumSize = codeFocusRedisProperties.getCacheConfig().getCaffeine().getMaximumSize();
		if(maximumSize>0){
			cacheBuilder.maximumSize(maximumSize);
		}
		return  new CaffeineCache(name,cacheBuilder.build());
	}

	public void clearLocal(String cacheName, Object key) {
		Cache cache = cacheMap.get(cacheName);
		if(cache == null) {
			return ;
		}

		RedisCaffeineCache redisCaffeineCache = (RedisCaffeineCache) cache;
		redisCaffeineCache.clearLocal(key);
	}
}

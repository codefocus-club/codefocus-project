package club.codefocus.framework.cache.cacheable;

import club.codefocus.framework.cache.handler.RedisHandler;
import club.codefocus.framework.cache.properties.CodeFocusRedisProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.core.TimeoutUtils;

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
public class CodeFocusCacheManager implements CacheManager {

	private ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>();

	private CodeFocusRedisProperties codeFocusRedisProperties;


	private boolean dynamic = true;

	RedisHandler redisHandler;


	public CodeFocusCacheManager(CodeFocusRedisProperties codeFocusRedisProperties, RedisHandler redisHandler
									 ) {
		super();
		this.codeFocusRedisProperties = codeFocusRedisProperties;
		this.dynamic = codeFocusRedisProperties.getCacheConfig().isDynamic();
		this.redisHandler=redisHandler;
	}


	/**
	 *
	 * @param name   key#100s/m/h/d
	 * @return
	 */
	@Override
	public Cache getCache(String name) {
		long expiration=0;
		String splitCode = codeFocusRedisProperties.getCacheConfig().getSplitCode();
		if(name.contains(splitCode)){
			String[] split = name.split(splitCode);
			if(split.length>1){
				try{
					String value = split[1];
					expiration=Integer.parseInt(value.replaceAll("[^0-9]",""));
					String unitStr = value.replaceAll("[^a-zA-z]","");
					log.debug("getCache unistr:{};expiration:{};value:{}",unitStr,expiration,value);
					TimeUnit unit=TimeUnit.SECONDS;
					if(!StringUtils.isEmpty(unitStr)){
						unitStr=unitStr.toLowerCase();
						if(unitStr.equals("s")){
							unit=TimeUnit.SECONDS;
						}else if(unitStr.equals("m")){
							unit=TimeUnit.MINUTES;
						}else if(unitStr.equals("h")){
							unit=TimeUnit.HOURS;
						}else if(unitStr.equals("d")){
							unit=TimeUnit.DAYS;
						}
					}
					expiration=TimeoutUtils.toSeconds(expiration, unit);
					log.debug("getCache unistr:{};expiration:{};value:{}",unitStr,expiration,value);
				}catch (Exception e){
					log.error(e.getMessage());
				}
			}
		}
		Cache cache = cacheMap.get(name);
		if(cache != null) {
			return cache;
		}
		cache = new CodeFocusCache(name,
				caffeineCache(name,expiration), codeFocusRedisProperties,expiration,redisHandler);
		log.debug("getCache name:{};expiration:{}",name,expiration);
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

		CodeFocusCache codeFocusCache = (CodeFocusCache) cache;
		codeFocusCache.clearLocal(key);
	}

}

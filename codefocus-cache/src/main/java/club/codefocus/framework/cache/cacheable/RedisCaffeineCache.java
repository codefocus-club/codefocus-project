package club.codefocus.framework.cache.cacheable;

import club.codefocus.framework.cache.handler.RedisHandler;
import club.codefocus.framework.cache.json.JSONException;
import club.codefocus.framework.cache.json.JSONObject;
import club.codefocus.framework.cache.properties.CodeFocusRedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Auther: jackl
 * @Date: 2019/11/13 13:10
 * @Description:
 */
@Slf4j
public class RedisCaffeineCache extends AbstractValueAdaptingCache {

	private String name;

	RedisHandler redisHandler;

	private CaffeineCache caffeineCache;

	private String cachePrefix;

	private long expiration = 0;

	private String topic;

	private Map<String, ReentrantLock> keyLockMap = new ConcurrentHashMap<String, ReentrantLock>();


	protected RedisCaffeineCache(boolean allowNullValues) {
		super(allowNullValues);
	}


	public RedisCaffeineCache(String name, CaffeineCache caffeineCache, CodeFocusRedisProperties codeFocusRedisProperties, long expiration,
							  RedisHandler redisHandler) {

		super(codeFocusRedisProperties.getCacheConfig().isCacheNullValues());
		this.name = name;
		this.caffeineCache = caffeineCache;
		this.cachePrefix = codeFocusRedisProperties.getCacheConfig().getCacheBaseName();
		this.expiration = expiration;
		this.topic=codeFocusRedisProperties.getCacheConfig().getCacheBaseName();
		this.redisHandler=redisHandler;
	}


	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Object getNativeCache() {
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object key, Callable<T> valueLoader) {
		log.info("get;key：{}",key);
		Object value = lookup(key);
		if(value != null) {
			return (T) value;
		}
		ReentrantLock lock = keyLockMap.get(key);
		if(lock == null) {
			lock = new ReentrantLock();
			keyLockMap.putIfAbsent(key.toString(), lock);
		}
		try {
			lock.lock();
			value = lookup(key);
			if(value != null) {
				return (T) value;
			}
			value = valueLoader.call();
			Object storeValue = toStoreValue(value);
			put(key, storeValue);
			return (T) value;
		} catch (Exception e) {
			throw new ValueRetrievalException(key, valueLoader, e.getCause());
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void put(Object key, Object value) {
		log.info("put; key:{};value:{}", key,value);
		if (!super.isAllowNullValues() && value == null) {
			this.evict(key);
            return;
        }
		String dataKey = getKey(key).toString();
		String dataValue = toStoreValue(value).toString();
		add(this.name,dataKey,dataKey,dataValue);

		push(this.name, getKey(key).toString());

		caffeineCache.put(dataKey, toStoreValue(value));
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		log.info("putIfAbsent; key:{},value:{}",key,value);
		Object cacheKey = getKey(key);
		Object prevValue = null;
		// 考虑使用分布式锁，或者将redis的setIfAbsent改为原子性操作
		synchronized (key) {
			prevValue = redisHandler.find(cacheKey.toString());
			if(prevValue == null) {
				String dataKey = getKey(key).toString();
				String dataValue = toStoreValue(value).toString();
				add(this.name,dataKey,dataKey,dataValue);
				push(this.name, dataKey);
				caffeineCache.put(cacheKey, toStoreValue(value));
			}
		}
		return toValueWrapper(prevValue);
	}


	@Override
	public void evict(Object key) {
		log.info("evict; key:{}",key);
		// 先清除redis中缓存数据，然后清除caffeine中的缓存，避免短时间内如果先清除caffeine缓存后其他请求会再从redis里加载到caffeine中
		String dataKey = getKey(key).toString();
		redisHandler.remove(dataKey);
		push(this.name, dataKey);
		caffeineCache.evict(key);
		String zsetKey = getZsetKey(this.name);
		redisHandler.opsForList().remove(zsetKey,0,dataKey);
	}

	@Override
	public void clear() {
		log.info("clear; name:{}",name);
		push(this.name, null);
		caffeineCache.clear();
		clearRedisData(name);
	}

	@Override
	protected Object lookup(Object key) {
		log.info(" lookup; key:{};name:{}",key,name);
		Object value=null;
		Object cacheKey = getKey(key);
		try{
			value = caffeineCache.get(key);
			if(( value instanceof SimpleValueWrapper)){
				value=((SimpleValueWrapper) value).get();
			}
			if(value != null) {
				return value;
			}
		}catch (Exception e){
			log.error(e.getMessage());
		}
		value=redisHandler.find(cacheKey.toString());
		log.info("lookup; cacheKey:{},value:{}",cacheKey,value);
		if(value != null) {
			caffeineCache.put(cacheKey, value);
		}
		return value;
	}

	private Object getKey(Object key) {
		return cachePrefix.concat(":").concat(name+":").concat(key.toString()).concat(":");
	}

	/**
	 * 缓存变更时通知其他节点清理本地缓存
	 */
	private void push(String name, String dataKey) {
		try {
			JSONObject jsonObject=new JSONObject();
			jsonObject.put("cacheName",name);
			jsonObject.put("key",dataKey);
			redisHandler.convertAndSend(topic,jsonObject.toString());
		} catch (JSONException e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * 清理本地缓存
	 * @param key
	 */
	public void clearLocal(Object key) {
		log.info("clearLocal  key:{};caffeineCache:{}", key,caffeineCache);
		if(key == null) {
			caffeineCache.clear();
		} else {
			caffeineCache.evict(key);
		}
	}
	String getZsetKey(String name){
		return cachePrefix.concat(":").concat(name);
	}

	/**
	 * 添加缓存redis
	 * @param key
	 * @param value
	 * @param dataKey
	 * @param dataValue
	 */
	public void add(String key, Object value,String dataKey,String dataValue) {
		StringBuilder sbu=new StringBuilder();
		sbu.append("redis.call('set',KEYS[1],ARGV[1]);");
		sbu.append("redis.call('expire', KEYS[1], ARGV[2]);");
		String zsetKey = getZsetKey(key);
		sbu.append("redis.call('lpush',KEYS[2],ARGV[3]);");
		sbu.append("redis.call('expire', KEYS[2], ARGV[4])");
		DefaultRedisScript LOCK_LUA_SCRIPT = new DefaultRedisScript<>(sbu.toString());
		List<String> keys =new ArrayList<>();
		keys.add(dataKey);
		keys.add(zsetKey);
		if(expiration==0){
			expiration=900;
		}
		redisHandler.execute(LOCK_LUA_SCRIPT,keys,dataValue,expiration,String.valueOf(value),expiration);

	}

	/**
	 * 清空redis
	 * @param key
	 */
	public void clearRedisData(String key){
		String zsetKey = getZsetKey(key);
		List dataKey = redisHandler.opsForList().range(zsetKey, 0, -1);
		int index=1;
		List<Object> keys =new ArrayList<>();
		StringBuilder sbu=new StringBuilder();
		String delKeys="";
		for(int i=0;i<dataKey.size();i++){
			String delKey = "KEYS[" + index + "]";
			if(StringUtils.isEmpty(delKeys)){
				delKeys= delKey;
			}else{
				delKeys=delKeys+","+delKey;
			}
			index++;
			keys.add(dataKey.get(i));
		}
		if(!StringUtils.isEmpty(delKeys)){
			sbu.append("redis.call('del',"+delKeys+");");
		}
		sbu.append("redis.call('ltrim',KEYS["+(index)+"],1,0);");
		DefaultRedisScript LOCK_LUA_SCRIPT = new DefaultRedisScript<>(sbu.toString());
		keys.add(zsetKey);
		log.info("clearRedisData keys:{},zsetKey:{}",keys.size(),sbu.toString());
		redisHandler.execute(LOCK_LUA_SCRIPT,keys);
	}
}

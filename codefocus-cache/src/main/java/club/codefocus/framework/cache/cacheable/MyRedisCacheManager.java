package club.codefocus.framework.cache.cacheable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: jackl
 * @Date: 2019/10/31 17:47
 * @Description:
 */
@Slf4j
public class MyRedisCacheManager extends RedisCacheManager {

    private String commonCacheName="cachable:redis:";

    private String commonGuavaCacheName="cachable:guava:";

    List<GuavaCache> guavaCacheList= Lists.newCopyOnWriteArrayList();

    public MyRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    @Autowired
    SimpleCacheManager simpleCacheManager;

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        String[] array = StringUtils.delimitedListToStringArray(name, "#");
        name = array[0];
        CacheBuilder<Object, Object> objectObjectCacheBuilder = CacheBuilder.newBuilder();
        objectObjectCacheBuilder.recordStats();
        if (array.length > 1) { // 解析TTL
            long ttl = Long.parseLong(array[1]);
            cacheConfig = getRedisCacheConfigurationWithTtl(ttl); // 注意单位我此处用的是秒，而非毫秒
            objectObjectCacheBuilder.expireAfterAccess(ttl,TimeUnit.SECONDS);
        }
        Cache<Object, Object> guavaCacheBuilder = objectObjectCacheBuilder.build();
        GuavaCache guavaCache = new GuavaCache(commonGuavaCacheName+name, guavaCacheBuilder);
        guavaCacheList.add(guavaCache);
        simpleCacheManager.setCaches(guavaCacheList);
        name=commonCacheName+name;
        return super.createRedisCache(name, cacheConfig);
    }

    private RedisCacheConfiguration getRedisCacheConfigurationWithTtl(Long seconds) {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
        redisCacheConfiguration = redisCacheConfiguration.serializeValuesWith(
                RedisSerializationContext
                        .SerializationPair
                        .fromSerializer(jackson2JsonRedisSerializer)
        ).entryTtl(Duration.ofSeconds(seconds));
        return redisCacheConfiguration;
    }

}

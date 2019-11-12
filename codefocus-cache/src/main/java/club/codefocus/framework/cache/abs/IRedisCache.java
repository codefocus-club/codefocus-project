package club.codefocus.framework.cache.abs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @Auther: jackl
 * @Date: 2019/10/25 10:26
 * @Description:IRedisCache
 */
public abstract class IRedisCache<K, V> {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 实体类类型
     */
    @SuppressWarnings("unused")
	private Class<V> entityClass;

    @Autowired
    protected RedisTemplate<K, V> redisTemplate;

    /**
     * 构造方法
     */
    @SuppressWarnings("unchecked")
    public IRedisCache() {
        ResolvableType resolvableType = ResolvableType.forClass(getClass());
        entityClass = (Class<V>) resolvableType.getSuperType().getGeneric().resolve();
    }

    public void put(K key, V entity) {
        BoundValueOperations<K, V> valueOperations = redisTemplate.boundValueOps((K) key);
        valueOperations.set((V) entity);
    }

    public void put(K key, V entity, long timeout) {
        this.put(key, entity, timeout, TimeUnit.MINUTES);
    }

    public void put(K key, V entity, long timeout, TimeUnit unit) {
        BoundValueOperations<K, V> valueOperations = redisTemplate.boundValueOps(key);
        valueOperations.set(entity, timeout, unit);
    }

    public V find(K key) {
        BoundValueOperations<K, V> valueOperations = redisTemplate.boundValueOps(key);
        return valueOperations.get();
    }

    public void remove(K key) {
        redisTemplate.delete(key);
    }

    public void increment(K key, long timeout, TimeUnit unit) {
        long count = redisTemplate.opsForValue().increment(key, 1);
        redisTemplate.expire(key, timeout, unit);
    }
}

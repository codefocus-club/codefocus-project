package club.codefocus.framework.cache.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
/**
 * @Auther: jackl
 * @Date: 2019/10/25 10:37
 * @Description:
 */
@Slf4j
public class RedisHandler<K,V> extends RedisTemplate<K,V>{

    /**
     * 获取锁
     * @param key  锁的key
     * @param time 锁的过期时间
     * @return
     */
    public boolean lock(K key, int time) {
        boolean execute = false;
        try {
            execute = (boolean) execute((RedisConnection connection) -> {
                synchronized (key.toString().intern()) {
                    StringRedisSerializer serializer = new StringRedisSerializer();
                    byte[] bk = serializer.serialize(key.toString());
                    Boolean b = connection.setNX(bk, bk);
                    connection.expire(bk, time);
                    connection.close();
                    return b;
                }
            });
        } catch (Exception e) {
            log.error("StringRedisCache lock error", e);
        }
        if (execute) {
            expire(key, time, TimeUnit.SECONDS);
        }
        return execute;
    }


    /**
     * 获取锁
     * @param key  锁的key
     * @param time 锁的过期时间
     * @return
     */
    @Deprecated
    public boolean getLockWhile(K key, int time) {
        boolean execute = false;
        while (true) {
            try {
                execute = (boolean) execute((RedisConnection connection) -> {
                    synchronized (key.toString().intern()) {
                        StringRedisSerializer serializer = new StringRedisSerializer();
                        byte[] bk = serializer.serialize(key.toString());
                        Boolean b = connection.setNX(bk, bk);
                        connection.expire(bk, time);
                        connection.close();
                        return b;
                    }
                });
            } catch (Exception e) {
                log.error("StringRedisCache getLockWhile error", e);
            }
            if (execute) {
                expire(key, time, TimeUnit.SECONDS);
                break;
            }
            try {
                sleep(500);
            } catch (InterruptedException e) {
                log.error("key:{};time:{};message:{}",key,time,e.getMessage());
            }
        }
        return execute;
    }

    /**
     * 释放锁
     * @param key
     */
    public void unlock(K key) {
        delete(key);
    }

    /**
     * 自增序列
     * @param key
     * @param timeout
     * @param unit
     */
    public void increment(K key, long timeout, TimeUnit unit) {
        opsForValue().increment(key, 1);
        expire(key, timeout, unit);
    }

    public V find(K key) {
        BoundValueOperations<K, V> valueOperations = boundValueOps(key);
        return valueOperations.get();
    }

    public void remove(K key) {
        delete(key);
    }
    public void put(K key, V entity) {
        BoundValueOperations<K, V> valueOperations = boundValueOps((K) key);
        valueOperations.set((V) entity);
    }

    public void put(K key, V entity, long timeout, TimeUnit unit) {
        BoundValueOperations<K, V> valueOperations = boundValueOps(key);
        valueOperations.set(entity, timeout, unit);
    }
}

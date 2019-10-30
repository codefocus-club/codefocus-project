package club.codefocus.framework.redis.service;

import club.codefocus.framework.redis.abs.IRedisCache;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
/**
 * @Auther: jackl
 * @Date: 2019/10/25 10:37
 * @Description:
 */
public class RedisLockHandler extends IRedisCache<String, String> {

    /**
     * 获取锁
     * @param key  锁的key
     * @param time 锁的过期时间
     * @return
     */
    public boolean lock(String key, int time) {
        boolean execute = false;
        try {
            execute = redisTemplate.execute((RedisConnection connection) -> {
                synchronized (key.intern()) {
                    StringRedisSerializer serializer = new StringRedisSerializer();
                    byte[] bk = serializer.serialize(key);
                    Boolean b = connection.setNX(bk, bk);
                    connection.expire(bk, time);
                    connection.close();
                    return b;
                }
            });
        } catch (Exception e) {
            logger.error("StringRedisCache lock error", e);
        }
        if (execute) {
            redisTemplate.expire(key, time, TimeUnit.SECONDS);
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
    public boolean getLockWhile(String key, int time) {
        boolean execute = false;
        while (true) {
            try {
                execute = redisTemplate.execute((RedisConnection connection) -> {
                    synchronized (key.intern()) {
                        StringRedisSerializer serializer = new StringRedisSerializer();
                        byte[] bk = serializer.serialize(key);
                        Boolean b = connection.setNX(bk, bk);
                        connection.expire(bk, time);
                        connection.close();
                        return b;
                    }
                });
            } catch (Exception e) {
                logger.error("StringRedisCache getLockWhile error", e);
            }
            if (execute) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
                break;
            }
            try {
                sleep(500);
            } catch (InterruptedException e) {
                logger.error("key:{};time:{};message:{}",key,time,e.getMessage());
            }
        }
        return execute;
    }

    /**
     * 释放锁
     *
     * @param key
     */
    public void unlock(String key) {
        redisTemplate.delete(key);
    }
}

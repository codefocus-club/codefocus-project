package club.codefocus.framework.cache.service;

import club.codefocus.framework.cache.abs.IRedisCache;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

/**
 * @Auther: jackl
 * @Date: 2019/10/25 10:37
 * @Description:
 */
public class RedisZSetHandler extends IRedisCache<String, String> {

    protected RedisTemplate<String, String> redisTemplate;

    public void add(String key, String value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    public Set<String> getByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    public double getEndTime(String key, String roomNum) {
        return redisTemplate.opsForZSet().score(key, roomNum);
    }

    public void removeByScore(String key, double min, double max) {
        redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    public void delJobByRoom(String key, String roomNum) {
        redisTemplate.opsForZSet().remove(key, roomNum);
    }
}

/*
package club.codefocus.framework.redis.abs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

*/
/**
 * @Auther: jackl
 * @Date: 2019/10/25 10:26
 * @Description:
 *//*

public abstract class IHashRedisCache<T> {

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    @Resource
    protected HashOperations<String, String, T> hashOperations;

    */
/**
     * 存入redis中的key
     *
     * @return
     *//*

    protected abstract String getRedisKey();

    */
/**
     * 添加
     *
     * @param key    key
     * @param doamin 对象
     *//*

    public void put(String key, T doamin) {
        this.put(key, doamin, -1);
    }

    */
/**
     * 添加
     * @param key    key
     * @param doamin 对象
     * @param expire 过期时间(单位:秒),传入 -1 时表示不设置过期时间
     *//*

    public void put(String key, T doamin, long expire) {
        hashOperations.put(getRedisKey(), key, doamin);
        if (expire != -1) {
            redisTemplate.expire(getRedisKey(), expire, TimeUnit.SECONDS);
        }
    }

    */
/**
     * 添加
     *
     * @param key      key
     * @param doamin   对象
     * @param expire   过期时间,传入 -1 时表示不设置过期时间
     * @param timeUnit 过期时间单位
     *//*

    public void put(String key, T doamin, long expire, TimeUnit timeUnit) {
        hashOperations.put(getRedisKey(), key, doamin);
        if (expire != -1) {
            redisTemplate.expire(getRedisKey(), expire, timeUnit);
        }
    }

    */
/**
     * 批量添加
     *
     * @param map 对象map
     *//*

    public void putAll(Map<String, T> map) {
        hashOperations.putAll(getRedisKey(), map);
    }

    */
/**
     * 删除
     *
     * @param key 传入key的名称
     *//*

    public void remove(String key) {
        hashOperations.delete(getRedisKey(), key);
    }

    */
/**
     * 查询
     *
     * @param key 查询的key
     * @return
     *//*

    public T get(String key) {
        return hashOperations.get(getRedisKey(), key);
    }

    */
/**
     * 获取当前redis库下所有对象
     *
     * @return
     *//*

    public List<T> getAll() {
        return hashOperations.values(getRedisKey());
    }

    */
/**
     * 查询查询当前redis库下所有key
     *
     * @return
     *//*

    public Set<String> getKeys() {
        return hashOperations.keys(getRedisKey());
    }

    */
/**
     * 判断key是否存在redis中
     *
     * @param key 传入key的名称
     * @return
     *//*

    public boolean isKeyExists(String key) {
        return hashOperations.hasKey(getRedisKey(), key);
    }

    */
/**
     * 查询当前key下缓存数量
     *
     * @return
     *//*

    public long count() {
        return hashOperations.size(getRedisKey());
    }

    */
/**
     * 清空redis
     *//*

    public void empty() {
        Set<String> set = hashOperations.keys(getRedisKey());
        set.stream().forEach(key -> hashOperations.delete(getRedisKey(), key));
    }

}
*/

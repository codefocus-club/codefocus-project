package club.codefocus.framework.redis.limit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分布式速率限制 例如:限制n秒钟请求x次
 * @ClassName: AccessSpeedLimit
 * @Description:
 * @author: jackl
 * @date: 2019/10/25 10:26
 */
@Slf4j
public class AccessSpeedLimit {

    private RedisTemplate<String, Serializable>  redisTemplate;

    public AccessSpeedLimit(RedisTemplate<String, Serializable> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    /**
     * 针对资源key,每seconds秒最多访问maxCount次,超过maxCount次返回false
     * @param key
     * @param seconds
     * @param limitCount
     * @return
     */
    public boolean tryAccess(String key, int seconds, int limitCount) {
        LimitRule limitRule = new LimitRule();
        limitRule.setLimitCount(limitCount);
        limitRule.setSeconds(seconds);
        return tryAccess(key, limitRule);
    }

    /**
     * 针对资源key,每limitRule.seconds秒最多访问limitRule.limitCount,超过limitCount次返回false
     * 超过lockCount 锁定lockTime
     * @param key
     * @param limitRule
     * @return
     */
    public boolean tryAccess(String key, LimitRule limitRule) {
        try{
            String newKey = "Limit:" + key;
            List<String> keys = new ArrayList<String>();
            keys.add(newKey);
            RedisScript<Number> redisScript = new DefaultRedisScript<>(buildLuaScript(), Number.class);
            Number execute = redisTemplate.execute(redisScript, keys, limitRule.getLimitCount(), limitRule.getSeconds());
            if(execute!=null && execute.intValue() <= limitRule.getLimitCount()){
                return true;
            }
        }catch (Exception e){
            log.error("key:{};msg:{}",key,e.getMessage());
        }
        return false;
    }


    private String buildLuaScript() {
        StringBuilder lua = new StringBuilder();
        lua.append("local c");
        lua.append("\nc = redis.call('get',KEYS[1])");
        // 调用不超过最大值，则直接返回
        lua.append("\nif c and tonumber(c) > tonumber(ARGV[1]) then");
        lua.append("\nreturn c;");
        lua.append("\nend");
        // 执行计算器自加
        lua.append("\nc = redis.call('incr',KEYS[1])");
        lua.append("\nif tonumber(c) == 1 then");
        // 从第一次调用开始限流，设置对应键值的过期
        lua.append("\nredis.call('expire',KEYS[1],ARGV[2])");
        lua.append("\nend");
        lua.append("\nreturn c;");
        return lua.toString();
    }
}

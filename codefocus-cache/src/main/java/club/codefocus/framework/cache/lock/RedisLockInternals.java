package club.codefocus.framework.cache.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @ClassName: RedisLockInternals
 * @Description:
 * @author: jackl
 * @date: 2018年5月20日 上午7:55:59
 * @Copyright: 智者开黑
 */
@Slf4j
class RedisLockInternals {

    private RedisTemplate redisTemplate;

    public RedisLockInternals(RedisTemplate<String, Serializable> redisTemplate, int lockTimeout) {
        this.lockTimeout=lockTimeout;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 重试等待时间
     */
    private int retryAwait = 300;

    private int lockTimeout;


    //定义获取锁的lua脚本
    private final static DefaultRedisScript<Long> LOCK_LUA_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then return redis.call('pexpire', KEYS[1], ARGV[2]) else return 0 end"
            , Long.class
    );



    //定义释放锁的lua脚本
    private final static DefaultRedisScript<Long> UNLOCK_LUA_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return -1 end"
            , Long.class
    );

    static final Long LOCK_SUCCESS = 1L;

    String tryRedisLock(String lockId, long time, TimeUnit unit) {
        final long startMillis = System.currentTimeMillis();
        final Long millisToWait = (unit != null) ? unit.toMillis(time) : null;
        String lockValue = null;
        while (lockValue == null) {
            lockValue = createRedisKey(lockId);
            if (lockValue != null) {
                break;
            }
            if (System.currentTimeMillis() - startMillis - retryAwait > millisToWait) {
                break;
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(retryAwait));
        }
        return lockValue;
    }

    private String createRedisKey(String key) {
        try {
            String value = key + randomId(1);
            //组装lua脚本参数
            List<String> keys = Arrays.asList(key);
            //执行脚本
            Object result = redisTemplate.execute(LOCK_LUA_SCRIPT, keys,value,lockTimeout);
            log.info("createRedisKey:{};value:{};result:{}",key,value,result);
            //存储本地变量
            if(LOCK_SUCCESS.equals(result)) {
                return value;
            }
        }catch (Exception e){
            log.error("createRedisKey key:{};msg:{}",key,e.getMessage());
        }
        return null;
    }

    void unlockRedisLock(String key, String value) {
        try {
            //组装lua脚本参数
            List<String> keys = Arrays.asList(key);
            // 使用lua脚本删除redis中匹配value的key，可以避免由于方法执行时间过长而redis锁自动过期失效的时候误删其他线程的锁
            redisTemplate.execute(UNLOCK_LUA_SCRIPT, keys, value);
            log.info("unlockRedisLock:{}",key,value);
        }catch (Exception e){
            log.error("unlockRedisLock key:{};msg:{}",key,e.getMessage());
        }
    }

    private final static char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
            'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z'};

    private String randomId(int size) {
        char[] cs = new char[size];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = digits[ThreadLocalRandom.current().nextInt(digits.length)];
        }
        return new String(cs);
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(300));
        System.out.println(System.currentTimeMillis());
    }
}

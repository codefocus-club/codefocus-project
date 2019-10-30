package club.codefocus.framework.redis.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.io.Serializable;
import java.util.ArrayList;
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

    private RedisTemplate<String, Serializable> redisTemplate;

    public RedisLockInternals(RedisTemplate<String, Serializable> redisTemplate, int lockTimeout) {
        this.lockTimeout=lockTimeout;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 重试等待时间
     */
    private int retryAwait = 300;

    private int lockTimeout;


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

    private String createRedisKey(String lockId) {
        try {
            String value = lockId + randomId(1);
            String luaScript = ""
                    + "\nlocal r = tonumber(redis.call('SETNX', KEYS[1],ARGV[1]));"
                    + "\nredis.call('PEXPIRE',KEYS[1],ARGV[2]);"
                    + "\nreturn r";
            List<String> keys = new ArrayList<String>();
            keys.add(lockId);
            List<String> args = new ArrayList<String>();
            args.add(value);
            args.add(lockTimeout + "");
            RedisScript<Number> redisScript = new DefaultRedisScript(luaScript, Long.class);
            Number execute = redisTemplate.execute(redisScript, keys, args);
            if (new Long(1).equals(execute)) {
                return value;
            }
        }catch (Exception e){
            log.error("lockId:{};msg:{}",lockId,e.getMessage());
        }
        return null;
    }

    void unlockRedisLock(String key, String value) {
        try {
            String luaScript = ""
                    + "\nlocal v = redis.call('GET', KEYS[1]);"
                    + "\nlocal r= 0;"
                    + "\nif v == ARGV[1] then"
                    + "\nr =redis.call('DEL',KEYS[1]);"
                    + "\nend"
                    + "\nreturn r";
            List<String> keys = new ArrayList<String>();
            keys.add(key);
            List<String> args = new ArrayList<String>();
            args.add(value);
            RedisScript<Number> redisScript = new DefaultRedisScript(luaScript, Long.class);
            redisTemplate.execute(redisScript, keys, args);
            args.add(value);
        }catch (Exception e){
            log.error("key:{};msg:{}",key,e.getMessage());
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

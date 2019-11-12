package club.codefocus.framework.cache.lock;

import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: RedisReentrantLock
 * @Description: redis 分布式锁
 * @author: jackl
 * @date: 2018年5月20日 上午7:56:14
 * @Copyright: 智者开黑
 */
public class RedisReentrantLock implements DistributedReentrantLock {

    private final ConcurrentMap<Thread, LockData> threadData = new ConcurrentHashMap<>();

    private RedisTemplate<String, Serializable> redisTemplate;

    private RedisLockInternals internals;

    private String lockId;

    public RedisReentrantLock(RedisTemplate<String, Serializable> redisTemplate, String lockId) {
        this(redisTemplate, lockId, 2000);//默认2秒后锁过期
    }

    public RedisReentrantLock(RedisTemplate<String, Serializable> redisTemplate, String lockId, int lockTimeout) {
        this.redisTemplate = redisTemplate;
        this.lockId = lockId;
        this.internals = new RedisLockInternals(redisTemplate, lockTimeout);
    }

    private static class LockData {
        final Thread owningThread;
        final String lockVal;
        final AtomicInteger lockCount = new AtomicInteger(1);

        private LockData(Thread owningThread, String lockVal) {
            this.owningThread = owningThread;
            this.lockVal = lockVal;
        }
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) {
        Thread currentThread = Thread.currentThread();
        LockData lockData = threadData.get(currentThread);
        if (lockData != null) {
            lockData.lockCount.incrementAndGet();
            return true;
        }
        String lockVal = internals.tryRedisLock(lockId, timeout, unit);
        if (lockVal != null) {
            LockData newLockData = new LockData(currentThread, lockVal);
            threadData.put(currentThread, newLockData);
            return true;
        }
        return false;
    }

    @Override
    public void unlock() {
        Thread currentThread = Thread.currentThread();
        LockData lockData = threadData.get(currentThread);
        if (lockData == null) {
            throw new IllegalMonitorStateException("You do not own the lock: " + lockId);
        }
        int newLockCount = lockData.lockCount.decrementAndGet();
        if (newLockCount > 0) {
            return;
        }
        if (newLockCount < 0) {
            throw new IllegalMonitorStateException("Lock count has gone negative for lock: " + lockId);
        }
        try {
            internals.unlockRedisLock(lockId, lockData.lockVal);
        } finally {
            threadData.remove(currentThread);
        }
    }
}

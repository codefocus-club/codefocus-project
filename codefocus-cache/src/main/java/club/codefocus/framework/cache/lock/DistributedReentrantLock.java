package club.codefocus.framework.cache.lock;

import java.util.concurrent.TimeUnit;

/**
 * @author  jackl
 * @since 1.0
 */
public interface DistributedReentrantLock {
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException;

    public void unlock();
}

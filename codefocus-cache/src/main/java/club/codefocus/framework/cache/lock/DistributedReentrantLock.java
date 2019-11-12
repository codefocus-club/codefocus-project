package club.codefocus.framework.cache.lock;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: DistributedReentrantLock
 * @Description:
 * @author: jackl
 * @date: 2018年5月20日 上午7:57:56
 * @Copyright: 智者开黑
 */
public interface DistributedReentrantLock {
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException;

    public void unlock();
}

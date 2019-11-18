package club.codefocus.framework.cache.annotation;

/**
 * @author  jackl
 * @Date: 2019/10/25 10:26
 * @Description:锁类型
 */
public enum LockType {
    /**
     * 方法锁
     */
    METHOD,
    /**
     * IP锁
     */
    IP,
    /**
     * USERID锁
     */
    UNIQUEID

}

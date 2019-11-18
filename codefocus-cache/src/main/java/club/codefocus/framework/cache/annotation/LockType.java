package club.codefocus.framework.cache.annotation;

/**
 * @author  jackl
 * @since 1.0
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

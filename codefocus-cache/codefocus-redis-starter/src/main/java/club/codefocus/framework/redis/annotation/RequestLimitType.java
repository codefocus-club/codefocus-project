package club.codefocus.framework.redis.annotation;

/**
 * @ClassName: LockType
 * @Description:锁类型
 * @author: jackl
 * @date: 2019/10/25 10:26
 */
public enum RequestLimitType {
    /**
     * header
     */
    HEADER,
    /**
     * request
     */
    REQUEST
}

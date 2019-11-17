package club.codefocus.framework.cache.annotation;

import java.lang.annotation.*;

/**
 * @author  jackl
 * @Date: 2019/10/25 10:26
 * @Description:分布式锁开关
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 分布式锁开关:true=开启;false=关闭
     */
    boolean open() default true;

    LockType lock() default LockType.IP;

    String field() default "";

    int expire() default 5000;

    int timeOut() default 3000;
}
/*
 * 使用分布式列子
 * @DistributedLock(lock = LockType.METHOD)
 */

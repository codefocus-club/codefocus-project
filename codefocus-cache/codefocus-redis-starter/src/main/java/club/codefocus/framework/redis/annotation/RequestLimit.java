package club.codefocus.framework.redis.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;


/**
 * @Title: RequestLimit
 * @Description: 限制方法调用次数（可以注解在方法或类）
 * @ProjectName:
 * @author: jackl
 * @date: 2019/10/25 10:26
 * @version: V1.0.0
 *  使用列子 接口
 *  @RequestLimit(limit = 3,period =1 ,unit = TimeUnit.MINUTES)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestLimit {

    /**
     * 限制上限
     */
    int limit() default 5;

    /**
     * 单位时间内
     */
    int period() default 1;

    /**
     * 时间单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 用户id
     * @return
     */
    String userId() default "";

    /**
     * 判断用户id具体的位置
     * @return
     */
    RequestLimitType userIdRequestLimitType() default RequestLimitType.REQUEST;

    /**
     * 客户唯一id
     * @return
     */
    String clientId() default "";

    /**
     * 客户唯一ID的类型
     * @return
     */
    RequestLimitType clientIdRequestLimitType() default RequestLimitType.REQUEST;

}

package club.codefocus.framework.cache.limit;

import lombok.Data;

/**
 * @author  jackl
 * @since 1.0
 */
@Data
public class LimitRule {

    /**
     * 单位时间
     */
    private int seconds;

    /**
     * 单位时间内限制的访问次数
     */
    private int limitCount;
}

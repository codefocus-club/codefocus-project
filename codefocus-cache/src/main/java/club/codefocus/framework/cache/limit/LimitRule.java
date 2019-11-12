package club.codefocus.framework.cache.limit;

import lombok.Data;

/**
 * 限制规则
 *
 * @ClassName: LimitRule
 * @Description:
 * @author: jackl
 * @date: 2019/10/25 10:26
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

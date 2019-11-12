package club.codefocus.framework.cache.limit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Auther= jackl
 * @Date= 2019/10/24 10=28
 * @Description=
 */
@Data
@ConfigurationProperties(prefix="spring.redis.code-focus")
public class CodeFocusRedisProperties {

    private  int globalLimitPeriodTime;
    private  int globalLimitCount;
    private boolean globalLimitOpen=false;

}


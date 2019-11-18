package club.codefocus.framework.cache.exception;

import lombok.Data;

/**
 * @author  jackl
 * @since 1.0
 */
@Data
public class RedisStarterDataView  {
    private Integer code;

    private String message;

    public RedisStarterDataView(RedisStarterExceptionEnum serverLimitException) {
        this.code=serverLimitException.getCode();
        this.message=serverLimitException.getMessage();

    }
}

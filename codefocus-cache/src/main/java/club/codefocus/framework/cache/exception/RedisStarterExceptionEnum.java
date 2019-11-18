package club.codefocus.framework.cache.exception;

/**
 * @author  jackl
 * @since 1.0
 */
public enum RedisStarterExceptionEnum {
    SERVER_METHOD_LOCKED (500102, "该方法正在执行,请勿重复操作"),
    SERVER_LIMIT_EXCEPTION (500101, "服务压力承载过大,稍后重试"),
    SERVER__EXCEPTION (500000, "服务异常"),
    RQUESTLIMITEXC_EPTION (500100, "接口访问频繁,稍后重试");

    private Integer code;

    private String message;

    RedisStarterExceptionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}

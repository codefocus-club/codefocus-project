package club.codefocus.framework.trace;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * @author youdw
 */
public class TraceIdUtil {


    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    public static String traceId() {
        return MDC.get(TraceConstant.TRACE_KEY);
    }
}

package club.codefocus.framework.trace;

import org.apache.dubbo.rpc.RpcContext;

import java.util.UUID;

/**
 * @author youdw
 */
public class TraceIdUtil {


    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    public static String get() {
        return RpcContext.getContext().getAttachment(TraceConstant.TRACE_KEY);
    }
}

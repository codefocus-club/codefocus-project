package club.codefocus.framework.trace.filter;

import club.codefocus.framework.trace.TraceConstant;
import club.codefocus.framework.trace.TraceIdUtil;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * web 端生成traceId
 * create at 2019/11/17 9:52 下午
 *
 * @author youdw
 */
public class WebProduceTraceIdFilter implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebProduceTraceIdFilter.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("entry traceIdFilter  url:{}",request.getRequestURL());
        }
        String traceId = TraceIdUtil.generate();
        RpcContext rpcContext = RpcContext.getContext();
        MDC.put(TraceConstant.TRACE_KEY, traceId);
        rpcContext.setAttachment(TraceConstant.TRACE_KEY, traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try{
        }finally {
            MDC.remove(TraceConstant.TRACE_KEY);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("traceId clear");
            }
        }
    }
}

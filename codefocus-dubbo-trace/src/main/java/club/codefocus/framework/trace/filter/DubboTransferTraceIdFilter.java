package club.codefocus.framework.trace.filter;

import club.codefocus.framework.trace.TraceConstant;
import club.codefocus.framework.trace.TraceIdUtil;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * dubbo SPI traceId调用拦截扩展
 *
 * @author youdw
 */
@Activate
public class DubboTransferTraceIdFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboTransferTraceIdFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation){
        RpcContext rpcContext = RpcContext.getContext();
        Result result =null;
        try{
            // traceId来源 web容器设置、非web容器
            String traceId = rpcContext.getAttachment(TraceConstant.TRACE_KEY);
            if (traceId == null) {
                if (MDC.get(TraceConstant.TRACE_KEY) != null) {
                    traceId = MDC.get(TraceConstant.TRACE_KEY);
                }else {
                    //重新生成
                    traceId = TraceIdUtil.generate();
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("DubboTraceIdFilter traceId={}", traceId);
            }
            //设置附加参数
            rpcContext.setAttachment(TraceConstant.TRACE_KEY, traceId);
            //设置mdc 用于日志打印
            MDC.put(TraceConstant.TRACE_KEY, traceId);
            result = invoker.invoke(invocation);
            if (rpcContext.isConsumerSide()) {
                LOGGER.debug(" consumer :{}"+traceId);
            }
            if(rpcContext.isProviderSide()){
                LOGGER.debug(" provider :{}"+traceId);
            }
        }catch (Exception e){
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        } finally{
            if (rpcContext.isProviderSide()) {
                //provider端调用完毕收移除mdc值 server端保留
                MDC.remove(TraceConstant.TRACE_KEY);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("traceId clear");
                }
            }
        }
        return result;
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        RpcContext rpcContext = RpcContext.getContext();
        try {
            String traceId = rpcContext.getAttachment(TraceConstant.TRACE_KEY);
            if (rpcContext.isConsumerSide()) {
                LOGGER.debug("onResponse  consumer :{}"+traceId);
            }
            if(rpcContext.isProviderSide()){
                LOGGER.debug("onResponse provider :{}"+traceId);
            }
            return  result;
        }finally {
            //provider端调用完毕收移除mdc值 server端保留
            MDC.remove(TraceConstant.TRACE_KEY);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("traceId clear");
            }
        }
    }
}

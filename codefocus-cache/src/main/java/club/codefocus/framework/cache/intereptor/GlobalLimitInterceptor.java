package club.codefocus.framework.cache.intereptor;

import club.codefocus.framework.cache.limit.AccessSpeedLimit;
import club.codefocus.framework.cache.properties.CodeFocusRedisProperties;
import club.codefocus.framework.cache.util.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * @author  jackl
 * @since 1.0
 */
@Slf4j
public class GlobalLimitInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    CodeFocusRedisProperties redisProperties;

    @Autowired
    RedisTemplate<String, Serializable>  limitRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean globalLimitOpen = redisProperties.isGlobalLimitOpen();
        log.debug("globalLimitOpen:{};",globalLimitOpen);
        if(globalLimitOpen){
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                String ip = IpUtils.getIpAddrExt(request);
                String methodName = handlerMethod.getMethod().getName();
                if (StringUtils.isNotBlank(ip) && StringUtils.isNotBlank(methodName)) {
                    AccessSpeedLimit accessSpeedLimit = new AccessSpeedLimit(limitRedisTemplate);
                    if (!accessSpeedLimit.tryAccess(ip, redisProperties.getGlobalLimitPeriodTime(), redisProperties.getGlobalLimitCount())) {
                        log.error("globalLimitOpen:{};globalLimitPeriodTime:{}",globalLimitOpen,redisProperties.getGlobalLimitPeriodTime());
                        throw new Exception("服务压力承载过大,稍后重试,或请调整服务配额");
                    }
                }
            }
        }
        return true;
    }
}

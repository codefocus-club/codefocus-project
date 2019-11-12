package club.codefocus.framework.cache.intereptor;

import club.codefocus.framework.cache.exception.RedisStarterDataView;
import club.codefocus.framework.cache.exception.RedisStarterExceptionEnum;
import club.codefocus.framework.cache.limit.AccessSpeedLimit;
import club.codefocus.framework.cache.limit.CodeFocusRedisProperties;
import club.codefocus.framework.cache.util.IpUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

/**
 * @ClassName: GlobalLimitInterceptor
 * @Description: 接口限流校验
 * @author: jackl
 * @date: 2019/10/25 10:26
 */
@Slf4j
public class GlobalLimitInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    CodeFocusRedisProperties redisProperties;

    @Autowired
    RedisTemplate<String, Serializable>  limitRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        boolean globalLimitOpen = redisProperties.isGlobalLimitOpen();
        if(globalLimitOpen){
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                String ip = IpUtils.getIpAddrExt(request);
                String methodName = handlerMethod.getMethod().getName();
                if (StringUtils.isNotBlank(ip) && StringUtils.isNotBlank(methodName)) {
                    AccessSpeedLimit accessSpeedLimit = new AccessSpeedLimit(limitRedisTemplate);
                    if (!accessSpeedLimit.tryAccess(ip, redisProperties.getGlobalLimitPeriodTime(), redisProperties.getGlobalLimitCount())) {
                        try {
                            RedisStarterDataView redisStarterDataView= new RedisStarterDataView(RedisStarterExceptionEnum.SERVER_LIMIT_EXCEPTION);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().print(JSONObject.toJSONString(redisStarterDataView));
                        } catch (IOException e) {
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

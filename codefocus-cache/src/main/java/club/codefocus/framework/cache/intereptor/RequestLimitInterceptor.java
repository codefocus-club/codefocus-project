package club.codefocus.framework.cache.intereptor;

import club.codefocus.framework.cache.annotation.RequestLimit;
import club.codefocus.framework.cache.annotation.RequestLimitType;
import club.codefocus.framework.cache.exception.RedisStarterDataView;
import club.codefocus.framework.cache.exception.RedisStarterExceptionEnum;
import club.codefocus.framework.cache.handler.RedisHandler;
import club.codefocus.framework.cache.util.IpUtils;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: RequestLimitInterceptor
 * @Description:单位时间内限流拦截器，根据@RequestLimit判断
 * @author: jackl
 * @date: 2019/10/25 10:26
 */
@Slf4j
public class RequestLimitInterceptor extends HandlerInterceptorAdapter {

    private final String PREFIX_REQUEST_LIMIT = "request.limit";

    @Autowired
    private RedisHandler redisHandler;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequestLimit requestLimit = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), RequestLimit.class);
            if (requestLimit == null) {
                requestLimit = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RequestLimit.class);
            }

            String ip = IpUtils.getIpAddrExt(request);
            String methodName = handlerMethod.getMethod().getName();

            if (requestLimit != null && StringUtils.isNotBlank(ip) && StringUtils.isNotBlank(methodName)) {

                String cacheKey =PREFIX_REQUEST_LIMIT + ":" + methodName + ":" + ip;

                setLid(requestLimit.userId(),requestLimit.userIdRequestLimitType(),request,cacheKey);

                setLid(requestLimit.clientId(),requestLimit.clientIdRequestLimitType(),request,cacheKey);

                Object cacheValue = redisHandler.find(cacheKey);
                int limit = requestLimit.limit();
                int period = requestLimit.period();
                TimeUnit unit = requestLimit.unit();
                if (cacheValue!=null && StringUtils.isNotBlank(cacheValue.toString())) {
                    Long countNum = NumberUtils.toLong(cacheValue.toString());
                    if (countNum >= limit) {
                        redisHandler.increment(cacheKey, period, unit);
                        try {
                            RedisStarterDataView redisStarterDataView= new RedisStarterDataView(RedisStarterExceptionEnum.RQUESTLIMITEXC_EPTION);
                            response.setContentType("application/json;charset=UTF-8");
                            ObjectMapper objectMapper=new ObjectMapper();
                            response.getWriter().print(objectMapper.writeValueAsString(redisStarterDataView));
                        } catch (IOException e) {
                        }
                        return false;
                    }
                }
                redisHandler.increment(cacheKey, period, unit);
            }
        }
        return true;
    }

    private void setLid( String lid,RequestLimitType requestLimitType,HttpServletRequest request,String cacheKey){
        if(!StringUtils.isEmpty(lid)){
            switch (requestLimitType) {
                case REQUEST:
                    Map<String, Object> paraMap = WebUtils.getParametersStartingWith(request, "");
                    lid = MapUtils.getString(paraMap, lid);
                    cacheKey=cacheKey+":"+lid;
                    break;
                case HEADER:
                    lid = request.getHeader(lid);
                    cacheKey=cacheKey+":"+lid;
                    break;
            }
        }
    }



}

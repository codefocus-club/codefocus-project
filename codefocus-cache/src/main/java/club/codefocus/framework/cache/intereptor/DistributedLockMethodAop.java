package club.codefocus.framework.cache.intereptor;

import club.codefocus.framework.cache.annotation.DistributedLock;
import club.codefocus.framework.cache.exception.RedisStarterDataView;
import club.codefocus.framework.cache.exception.RedisStarterExceptionEnum;
import club.codefocus.framework.cache.lock.RedisReentrantLock;
import club.codefocus.framework.cache.util.IpUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: DistributedLockMethodAop
 * @Description:
 * @author: jackl
 * @date: 2019/10/25 10:26
 */
@Slf4j
@Aspect
@Component
public class DistributedLockMethodAop {

    private final static Long CONNECTION_TIMEOUT = 3000L;


    @Resource
    private HttpServletRequest request;

    @Autowired
    RedisTemplate<String, Serializable> limitRedisTemplate;


    @Pointcut(value = "@annotation(club.codefocus.framework.cache.annotation.DistributedLock)")
    public void pointcutDistributedLockMethod() {

    }

    @Around("pointcutDistributedLockMethod()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Object retVal = null;
        DistributedLock distributedLock = null;
        String lockName = null;
        try {
            Signature signature = pjp.getSignature();
            MethodSignature method = (MethodSignature) signature;
            distributedLock = AnnotationUtils.findAnnotation(method.getMethod(), DistributedLock.class);
        } catch (Exception e) {

        }
        final RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        final ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletResponse response = sra.getResponse();
        if (distributedLock != null) {
            if (distributedLock.open()) {
                lockName = getLockName(distributedLock) + pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();
                RedisReentrantLock lock = new RedisReentrantLock(limitRedisTemplate, lockName, distributedLock.expire());
                try {
                    if (lock.tryLock(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)) {
                        log.info("获取分布式锁:{}", lockName);
                        retVal = pjp.proceed();
                    } else {
                        log.error("获取分布式锁超时,锁已被占用:{}", lockName);
                        try {
                            RedisStarterDataView redisStarterDataView= new RedisStarterDataView(RedisStarterExceptionEnum.SERVER_METHOD_LOCKED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().print(JSONObject.toJSONString(redisStarterDataView));
                        } catch (IOException e) {
                        }
                    }
                } catch (Exception e) {
                    throw e;
                } finally {
                    try {
                        lock.unlock();
                        log.info("释放分布式锁:{}", lockName);
                    } catch (Exception e) {

                    }
                }
            } else {
                retVal = pjp.proceed();
            }

        } else {
            retVal = pjp.proceed();
        }
        return retVal;
    }

    private String getLockName(DistributedLock distributedLock) {
        final RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        final ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        StringBuffer stringBuffer = new StringBuffer("dislock:");
        switch (distributedLock.lock()) {
            case IP:
                stringBuffer.append(IpUtils.getIpAddrExt(request) + ":");
                break;
            case UNIQUEID:
                HttpServletRequest request = sra.getRequest();
                if (StringUtils.isNotBlank(distributedLock.field())) {
                    String uniqueId = request.getParameter(distributedLock.field());
                    if (StringUtils.isNotBlank(uniqueId)) {
                        stringBuffer.append(uniqueId + ":");
                    }
                }
                break;
        }
        return stringBuffer.toString();
    }
}

package club.codefocus.framework.cache.config;


import club.codefocus.framework.cache.cacheable.CacheMessageListener;
import club.codefocus.framework.cache.cacheable.CodeFocusCacheManager;
import club.codefocus.framework.cache.intereptor.GlobalLimitInterceptor;
import club.codefocus.framework.cache.intereptor.RequestLimitInterceptor;
import club.codefocus.framework.cache.properties.CodeFocusRedisProperties;
import club.codefocus.framework.cache.handler.RedisHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.Serializable;

/**
 * @Auther: jackl
 * @Date: 2019/10/25 10:26
 * @Description:
 */
@Slf4j
@EnableCaching  //开启缓存
@ComponentScan("club.codefocus.framework.cache")
@Configuration
@EnableConfigurationProperties(CodeFocusRedisProperties.class)
public class CodeFocusRedisConfig extends CachingConfigurerSupport implements WebMvcConfigurer {


    @Autowired
    CodeFocusRedisProperties codeFocusRedisProperties;

    @Bean
    public RedisTemplate<String, Serializable> limitRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Serializable> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }


    @Bean
    RedisHandler redisHandler(LettuceConnectionFactory redisConnectionFactory) {
        RedisHandler redisHandler = new RedisHandler();
        redisHandler.setKeySerializer(new StringRedisSerializer());
        redisHandler.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisHandler.setHashKeySerializer(new StringRedisSerializer());
        redisHandler.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisHandler.setConnectionFactory(redisConnectionFactory);
        return redisHandler;
    }

    @Bean
    RequestLimitInterceptor requestLimitInterceptor() {
        return new RequestLimitInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalLimitInterceptor());
        registry.addInterceptor(requestLimitInterceptor());
    }

    @Bean
    GlobalLimitInterceptor globalLimitInterceptor() {
        return new GlobalLimitInterceptor();
    }


    @Bean
    public CodeFocusCacheManager redisCaffeineCacheManager(LettuceConnectionFactory redisConnectionFactory) {
        return new CodeFocusCacheManager(codeFocusRedisProperties, redisHandler(redisConnectionFactory));
    }


    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisHandler redisHandler,
                                                                       CodeFocusCacheManager codeFocusCacheManager) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisHandler.getConnectionFactory());
        CacheMessageListener cacheMessageListener = new CacheMessageListener(codeFocusCacheManager);
        redisMessageListenerContainer.addMessageListener(cacheMessageListener, new ChannelTopic(codeFocusRedisProperties.getCacheConfig().getCacheBaseName()));
        return redisMessageListenerContainer;
    }

    /**
     * 自定义缓存key生成策略
     * 使用方法 @Cacheable(keyGenerator="keyGenerator")
     *
     * @return
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                if (obj != null) {
                    sb.append(obj.toString().hashCode());
                }
            }
            return sb.toString();
        };
    }


}

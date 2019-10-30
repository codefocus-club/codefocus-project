package club.codefocus.framework.redis.config;

import club.codefocus.framework.redis.intereptor.GlobalLimitInterceptor;
import club.codefocus.framework.redis.intereptor.RequestLimitInterceptor;
import club.codefocus.framework.redis.limit.CodeFocusRedisProperties;
import club.codefocus.framework.redis.service.RedisLockHandler;
import club.codefocus.framework.redis.service.RedisStringHandler;
import club.codefocus.framework.redis.service.RedisZSetHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
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
@ComponentScan("club.codefocus.framework.redis")
@Configuration
@EnableConfigurationProperties(CodeFocusRedisProperties.class)
public class CodeFocusRedisConfig implements WebMvcConfigurer {


    @Bean
    public RedisTemplate<String, Serializable> limitRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Serializable> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

   @Bean
   RedisLockHandler redisLockHandler(){
       return new RedisLockHandler();
   }
   @Bean
   RedisStringHandler redisStringHandler(){
       return new RedisStringHandler();
   }
   @Bean
   RedisZSetHandler redisZSetHandler(){
       return new RedisZSetHandler();
   }

  /* @Bean
   DistributedLockMethodAop distributedLockMethodAop(){
       return new DistributedLockMethodAop();
   }*/

   @Bean
   RequestLimitInterceptor requestLimitInterceptor(){
       return new RequestLimitInterceptor();
   }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalLimitInterceptor());
        registry.addInterceptor(requestLimitInterceptor());
    }
   @Bean
   GlobalLimitInterceptor globalLimitInterceptor(){
       return new GlobalLimitInterceptor();
   }
}

package club.codefocus.framework.cache.properties;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author  jackl
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix="spring.redis.code-focus")
public class CodeFocusRedisProperties {

    private  int globalLimitPeriodTime;

    private  int globalLimitCount;

    private boolean globalLimitOpen=false;

    private CacheConfig cacheConfig=new CacheConfig();

    @Data
    public class CacheConfig{

        /** 是否存储空值，默认true，防止缓存穿透*/
        private boolean cacheNullValues = true;

        /** 是否动态根据cacheName创建Cache的实现，默认true*/
        private boolean dynamic = true;

        private String cacheBaseName="";

        Caffeine caffeine=new Caffeine();

        private String splitCode="#";

        @Data
        public class Caffeine{
            /** 初始化大小*/
            private int initialCapacity;

            /** 最大缓存对象个数，超过此数量时之前放入的缓存将失效*/
            private long maximumSize;
        }
        public String getCacheBaseName(){
            return cacheBaseName+":cache";
        }
    }

}


# codefocus-dubbo-trace 自定义分布式TraceId

## 亮点：

        1.采用UUID策略生成traceId
        
        2.支持定义生成TraceId
        
        3.主异线程切换TraceId不丢失
        

## 环境依赖：
    springboot 2.1.3.RELEASE 版本
    
### (step 1)使用教程：

            <dependency>
                <groupId>club.codefocus.framework</groupId>
                <artifactId>codefocus-dubbo-trace</artifactId>
                <version>1.0.1-SNAPSHOT</version>
            </dependency>
            
### (step 2)YML配置详解：

            dubbo:
              provider:
                filter: dubboTraceIdFilter
              consumer:
                filter: dubboTraceIdFilter
                
### (step 3)TraceIdUtil：

            TraceIdUtil.traceId() 获取分布式TraceId    
                            
### (step 4) logback-spring.xml || logback.xml：
            
  ###### xml获取traceId的方式：
  
            %X{codefocus-trace}
             
  ###### logback-spring.xml：    
  
             <?xml version="1.0" encoding="UTF-8"?>
             <configuration>
                 <property name="NORMAL_PATTERN"
                           value="%d{ISO8601} [%-16.16thread] %-5level -- %-50.50logger - %M [%4line] --provider %X{codefocus-trace}  | %msg%n"/>
             </configuration>
            
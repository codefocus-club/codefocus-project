# codefocus-dubbo-trace 自定义分布式TraceId

## 亮点：

1. 采用UUID策略生成traceId

2. 支持定义生成TraceId

3. 主异线程切换TraceId不丢失
        

## 环境依赖：
    springboot 2.1.3.RELEASE 版本
    
## 使用教程：
### (step 1) 引入依赖
```xml
<dependency>
    <groupId>club.codefocus.framework</groupId>
    <artifactId>codefocus-dubbo-trace</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>

```
             
### (step 2)YML配置详解：
```yaml

dubbo:
  provider:
    filter: dubboTraceIdFilter
  consumer:
    filter: dubboTraceIdFilter
 
 ```               
### (step 3)TraceIdUtil：
```java
TraceIdUtil.traceId() 获取分布式TraceId

```       
### (step 4) logback-spring.xml || logback.xml：
            
  ###### xml获取traceId的方式：
  ```xml
  %X{codefocus-trace}
  ```           
  ###### logback-spring.xml：    
 ```xml
<?xml version="1.0" encoding="UTF-8"?>
 <configuration>
     <property name="NORMAL_PATTERN"
               value="%d{ISO8601} [%-16.16thread] %-5level -- %-50.50logger - %M [%4line] --provider %X{codefocus-trace}  | %msg%n"/>
 </configuration>
```
            
## 主异线程切换TraceId不丢失配置
MDC使用的是ThreadLocal实现 子线程无法传递mdc值 可以配置MdcThreadPoolTaskExecutor实现mdc值传递（**默认启用**）
    
```yaml
# 默认使用spring.task.execution配置 配置示例
spring: 
  task:
    execution:
      core-pool-size: 80
      max-pool-size: 200
      pool:
        queue-capacity: 1000
```

自定义实现需要使用MdcThreadPoolTaskExecutor声明Bean 示例如下：
 ```java
@Bean
public ThreadPoolTaskExecutor taskExecutor() {
    log.debug("mdcThreadPoolTaskExecutor init");
    ThreadPoolTaskExecutor executor = new MdcThreadPoolTaskExecutor();
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
}
```
    
    

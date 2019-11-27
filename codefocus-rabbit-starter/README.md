# codefocus-rabbit-starter 基于YML动态配置创建Rabbit


## 亮点：

        1.动态创建rabbit
        
        2.支持自定义配置
        
        
        
## 环境依赖：
    springboot 2.1.3.RELEASE 版本
    
### (step 1)使用教程：
```xml

<dependency>
    <groupId>club.codefocus.framework</groupId>
    <artifactId>codefocus-rabbit-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
 </dependency>

```

### (step 2)YML配置详解

```yaml

spring:
      rabbitmq:
        host: 127.0.0.1
        port: 5672
        username: guest
        password: guest
        listener:
          simple:
          #全局配置：公平派遣消息-消费者无应答并发消费数，默认为循环派遣
            prefetch: 2
            retry:
              #开启消费者重试-考虑幂等处理
              enabled: true
              #重试间隔时间ms
              initial-interval: 3000
              #最大重试次数
              max-attempts: 2
            #采用手动回答
            acknowledge-mode: manual
        ##以上通用注解
        dynamic-message:
          #动态配置队列
          dynamic-queue-list:
             #交换机
           - dynamic-exchange-name: fac.direct.exchange
             #当前队列开关
             enabled: true
             #none/auto/manual
             ack-nowledge-mode: manual
             #direct/topic/fanout
             exchange-type: fanout
             #消费Bean实例（名称即可）
             bean-name: dynamicConsumer
             #队列名称
             queue-name: jackl
             #route
             routing-key: jackl123
             #是否自动删除队列
             auto-delete: false
             #是否持久化
             durable: true
             #交换机
           - dynamicexchangename: fac.direct.exchange
             enabled: true
             #none/auto/manual
             ackNowledgeMode: manual
             #direct/topic/fanout
             exchange-type: topic
             beanname: dynamicConsumerA
             queuename: jackl
             routingkey: qqqq
             #是否自动删除队列
             autodelete: false
             #是否持久化
             durable: true

```

### (step 3)MQ消费Bean实例：

```java

package com.example.demo.consumer;
    
import club.codefocus.framework.rabbit.api.AbsMQConsumerService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
    
    
@Component
public class DynamicConsumer extends AbsMQConsumerService {
    volatile int index=1;

    @Override
    public boolean process(Message message, Channel channel) {
        System.out.println(
                index+"=============DynamicConsumer[" +  new String(message.getBody()));
        index++;
        return true;
    }

}
```
    


### (step 4)注入MQDynamicHandler 实例：

```java
@Autowired
private MQDynamicHandler mQDynamicHandler;
String EXCHANGE="";//交换机名称
String routingKey=""://
String msg="";//发送消息
mQDynamicHandler.publishMsg(EXCHANGE, routingKey,msg);

```

    
    
## 注意：
 1.DynamicConsumer 需要继承AbsMQConsumerService
 

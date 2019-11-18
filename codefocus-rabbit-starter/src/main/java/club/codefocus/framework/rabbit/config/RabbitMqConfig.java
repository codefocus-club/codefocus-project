package club.codefocus.framework.rabbit.config;

import club.codefocus.framework.rabbit.api.MQDynamicHandler;
import club.codefocus.framework.rabbit.enums.EnumsExchangeType;
import club.codefocus.framework.rabbit.enums.RabbitProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author  jackl
 */
@Slf4j
@ComponentScan("club.codefocus.framework.rabbit")
@Configuration
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitMqConfig {



    @Autowired RabbitProperties rabbitProperties;

    @Autowired
    MQDynamicHandler mqDynamicHandler;

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitInitHandler rabbitInitHandler(){
        List<RabbitProperties.RabbitQueue> dynamicQueueList = rabbitProperties.getDynamicQueueList();
        for (RabbitProperties.RabbitQueue rabbitQueue : dynamicQueueList) {
            log.info("rabbitInitHandler:{}",rabbitQueue.toString());
            Boolean enabled = rabbitQueue.getEnabled();
            if(enabled){
                String exchangeTypeStr = rabbitQueue.getExchangeType();
                EnumsExchangeType exchangeType=null;
                if(exchangeTypeStr.equals("direct")){
                    exchangeType=EnumsExchangeType.DEFAULT;
                }else if(exchangeTypeStr.equals("topic")){
                    exchangeType=EnumsExchangeType.DEFAULT;
                }else if(exchangeTypeStr.equals("fanout")){
                    exchangeType=EnumsExchangeType.FANOUT;
                }
                try {
                    mqDynamicHandler.consumerGenerate(rabbitQueue.getBeanName(),rabbitQueue.getDynamicExchangeName(),
                            rabbitQueue.getQueueName(),rabbitQueue.getRoutingKey(),rabbitQueue.getAutoDelete(),
                            rabbitQueue.getDurable(),rabbitQueue.getAckNowledgeMode(), exchangeType);
                    log.info("queueName:{};routingKey:{};is success",rabbitQueue.getQueueName(),rabbitQueue.getRoutingKey());
                } catch (Exception e) {
                    log.info("queueName:{};routingKey:{};is fail",rabbitQueue.getQueueName(),rabbitQueue.getRoutingKey());
                }
            }
        }
        return new RabbitInitHandler();
    }


}


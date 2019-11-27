package club.codefocus.framework.rabbit.api;

import club.codefocus.framework.rabbit.enums.EnumsExchangeType;
import club.codefocus.framework.rabbit.enums.RabbitProperties;
import club.codefocus.framework.rabbit.factory.MQContainerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

/**
 * @author  jackl
 */
@Slf4j
public class MQDynamicHandler implements ApplicationContextAware, CommandLineRunner {
    private static ApplicationContext applicationContext;

    AmqpTemplate amqpTemplate;
    ConnectionFactory connectionFactory;
    RabbitAdmin rabbitAdmin;
    RabbitProperties rabbitProperties;

    public MQDynamicHandler(ConnectionFactory connectionFactory, AmqpTemplate amqpTemplate, RabbitAdmin rabbitAdmin,RabbitProperties rabbitProperties) {
        this.connectionFactory=connectionFactory;
        this.amqpTemplate=amqpTemplate;
        this.rabbitAdmin=rabbitAdmin;
        this.rabbitProperties=rabbitProperties;
    }

    public void publishMsg(String exchange, String routingKey, Object msg) {
        amqpTemplate.convertAndSend(exchange, routingKey, msg);
    }

    public void consumerGenerate(String beanName,String exchangeName, String queueName, String routingKey, boolean autoDelete, boolean durable,
                                String acknowledgeMode, EnumsExchangeType exchangeType) throws Exception {
        AbsMQConsumerService absMQConsumerService=null;
        try {
            Object bean = this.getBean(beanName);
            absMQConsumerService=(AbsMQConsumerService) bean;
            log.debug("consumerGenerate:{}",bean);
        }catch (Exception e){
            log.debug("consumer bean is null");
        }
        MQContainerFactory fac =
                MQContainerFactory.builder().directExchange(exchangeName).queue(queueName).autoDeleted(autoDelete)
                        .acknowledgeMode(acknowledgeMode).durable(durable).routingKey(routingKey).rabbitAdmin(rabbitAdmin)
                        .connectionFactory(connectionFactory).absMQConsumerService(absMQConsumerService).exchangeType(exchangeType).build();
        fac.getObject();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    public Object getBean(String beanName) {
        return applicationContext != null?applicationContext.getBean(beanName):null;
    }

    @Override
    public void run(String... args) throws Exception {
        List<RabbitProperties.RabbitQueue> dynamicQueueList = rabbitProperties.getDynamicQueueList();
        for (RabbitProperties.RabbitQueue rabbitQueue : dynamicQueueList) {
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
                    consumerGenerate(rabbitQueue.getBeanName(),rabbitQueue.getDynamicExchangeName(),
                            rabbitQueue.getQueueName(),rabbitQueue.getRoutingKey(),rabbitQueue.getAutoDelete(),
                            rabbitQueue.getDurable(),rabbitQueue.getAckNowledgeMode(), exchangeType);
                    log.debug("queueName:{};routingKey:{};is success",rabbitQueue.getQueueName(),rabbitQueue.getRoutingKey());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("queueName:{};routingKey:{};is fail",rabbitQueue.getQueueName(),rabbitQueue.getRoutingKey());
                }
            }
        }
        log.debug("MQDynamicHandler:{}",applicationContext);
    }
}

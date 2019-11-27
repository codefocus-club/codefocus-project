package club.codefocus.framework.rabbit.api;

import club.codefocus.framework.rabbit.enums.EnumsExchangeType;
import club.codefocus.framework.rabbit.factory.MQContainerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author  jackl
 */
@Component
public class MQDynamicHandler implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    @Autowired
    AmqpTemplate amqpTemplate;

    @Autowired
    ConnectionFactory connectionFactory;

    @Autowired
    RabbitAdmin rabbitAdmin;



    public void publishMsg(String exchange, String routingKey, Object msg) {
        amqpTemplate.convertAndSend(exchange, routingKey, msg);
    }

    public void consumerGenerate(String beanName,String exchangeName, String queueName, String routingKey, boolean autoDelete, boolean durable,
                                 String acknowledgeMode, EnumsExchangeType exchangeType) throws Exception {
        Object bean = this.getBean(beanName);
        MQContainerFactory fac =
                MQContainerFactory.builder().directExchange(exchangeName).queue(queueName).autoDeleted(autoDelete)
                        .acknowledgeMode(acknowledgeMode).durable(durable).routingKey(routingKey).rabbitAdmin(rabbitAdmin)
                        .connectionFactory(connectionFactory).consumer((AbsMQConsumerService) bean).exchangeType(exchangeType).build();
        fac.getObject();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    public Object getBean(String beanName) {
        return applicationContext != null?applicationContext.getBean(beanName):null;
    }
}

package club.codefocus.framework.rabbit.api;

import club.codefocus.framework.rabbit.config.ApplicationContextHelper;
import club.codefocus.framework.rabbit.enums.EnumsExchangeType;
import club.codefocus.framework.rabbit.factory.MQContainerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author  jackl
 */
@Component
public class MQDynamicHandler {

    @Autowired
    AmqpTemplate amqpTemplate;

    @Autowired
    ConnectionFactory connectionFactory;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    ApplicationContextHelper applicationContextHelper;

    /**
     * 将消息发送到指定的交换器上
     * @param exchange
     * @param routingKey
     * @param msg
     */
    public void publishMsg(String exchange, String routingKey, Object msg) {
        amqpTemplate.convertAndSend(exchange, routingKey, msg);
    }

    /**
     *
     * @param beanName
     * @param exchangeName
     * @param queueName
     * @param routingKey
     * @param autoDelete
     * @param durable
     * @param acknowledgeMode
     * @param exchangeType
     * @return
     * @throws Exception
     */
    public void consumerGenerate(String beanName,String exchangeName, String queueName, String routingKey, boolean autoDelete, boolean durable,
                                 String acknowledgeMode, EnumsExchangeType exchangeType) throws Exception {
        Object bean = applicationContextHelper.getBean(beanName);
        MQContainerFactory fac =
                MQContainerFactory.builder().directExchange(exchangeName).queue(queueName).autoDeleted(autoDelete)
                        .acknowledgeMode(acknowledgeMode).durable(durable).routingKey(routingKey).rabbitAdmin(rabbitAdmin)
                        .connectionFactory(connectionFactory).consumer((AbsMQConsumerService) bean).exchangeType(exchangeType).build();
        fac.getObject();
    }

}

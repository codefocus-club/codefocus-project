package club.codefocus.framework.rabbit.factory;

import club.codefocus.framework.rabbit.api.AbsMQConsumerService;
import club.codefocus.framework.rabbit.enums.EnumsExchangeType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.StringUtils;
/**
 * @author  jackl
 */
@Slf4j
@Data
@Builder
public class MQContainerFactory implements FactoryBean<SimpleMessageListenerContainer> {
    private EnumsExchangeType exchangeType;

    private String directExchange;
    private String topicExchange;
    private String fanoutExchange;

    private String queue;
    private String routingKey;


    private Boolean autoDeleted;
    private Boolean durable;
    private String acknowledgeMode;

    private ConnectionFactory connectionFactory;
    private RabbitAdmin rabbitAdmin;
    private Integer concurrentNum;
    private Integer prefetchCount;
    // 消费方
    private AbsMQConsumerService consumer;


    private String listenerId;


    private Exchange buildExchange() {
        if (directExchange != null) {
            exchangeType = EnumsExchangeType.DIRECT;
            return new DirectExchange(directExchange);
        } else if (topicExchange != null) {
            exchangeType = EnumsExchangeType.TOPIC;
            return new TopicExchange(topicExchange);
        } else if (fanoutExchange != null) {
            exchangeType = EnumsExchangeType.FANOUT;
            return new FanoutExchange(fanoutExchange);
        } else {
            if (StringUtils.isEmpty(routingKey)) {
                throw new IllegalArgumentException("defaultExchange's routingKey should not be null!");
            }
            exchangeType = EnumsExchangeType.DEFAULT;
            return new DirectExchange("");
        }
    }


    private Queue buildQueue() {
        if (StringUtils.isEmpty(queue)) {
            throw new IllegalArgumentException("queue name should not be null!");
        }

        return new Queue(queue, durable == null ? false : durable, false, autoDeleted == null ? true : autoDeleted);
    }


    private Binding bind(Queue queue, Exchange exchange) {
        return exchangeType.binding(queue, exchange, routingKey);
    }


    private void check() {
        if (rabbitAdmin == null || connectionFactory == null) {
            throw new IllegalArgumentException("rabbitAdmin and connectionFactory should not be null!");
        }
    }


    @Override
    public SimpleMessageListenerContainer getObject() throws Exception {
        check();

        Queue queue = buildQueue();
        Exchange exchange = buildExchange();
        Binding binding = bind(queue, exchange);

        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareBinding(binding);


        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setAmqpAdmin(rabbitAdmin);
        container.setConnectionFactory(connectionFactory);
        container.setQueues(queue);
        container.setPrefetchCount( prefetchCount == null ? 20 : prefetchCount);

        container.setConcurrentConsumers(concurrentNum == null ? 1 : concurrentNum);
        if(!StringUtils.isEmpty(acknowledgeMode)){
            if(acknowledgeMode.contains("none")){
                container.setAcknowledgeMode(AcknowledgeMode.NONE);
            }else if(acknowledgeMode.contains("auto")){
                container.setAcknowledgeMode(AcknowledgeMode.AUTO);
            }else if(acknowledgeMode.contains("manual")){
                container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
            }else{
                container.setAcknowledgeMode(AcknowledgeMode.NONE);
            }
        }else{
            container.setAcknowledgeMode(AcknowledgeMode.NONE);
        }
        if (consumer != null) {
            container.setMessageListener(consumer);
            container.start();
            this.listenerId = container.getListenerId();
            log.info("queue:{};listenerId:{}",queue,listenerId);
            this.setListenerId(container.getListenerId());
        }
        return container;
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleMessageListenerContainer.class;
    }
}

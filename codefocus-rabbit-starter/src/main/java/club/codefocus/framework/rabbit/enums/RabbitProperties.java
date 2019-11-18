package club.codefocus.framework.rabbit.enums;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @auther jackl
 */
@Data
@ConfigurationProperties(prefix="spring.rabbitmq.dynamic-message")
public class RabbitProperties {

    private List<RabbitQueue> dynamicQueueList;

    @Data
    public static class  RabbitQueue {
        private String dynamicExchangeName;
        private String exchangeType;
        private String beanName;
        private String queueName;
        private String routingKey;
        private Boolean autoDelete;
        private Boolean durable;
        private String ackNowledgeMode;
        private Boolean enabled=true;
    }


}


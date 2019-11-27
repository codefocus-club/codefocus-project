package club.codefocus.framework.rabbit.config;

import club.codefocus.framework.rabbit.api.MQDynamicHandler;
import club.codefocus.framework.rabbit.enums.EnumsExchangeType;
import club.codefocus.framework.rabbit.enums.RabbitProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
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

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public MQDynamicHandler mQDynamicHandler(AmqpTemplate amqpTemplate,ConnectionFactory connectionFactory,RabbitProperties rabbitProperties){
        MQDynamicHandler mqDynamicHandler = new MQDynamicHandler(connectionFactory, amqpTemplate,  rabbitAdmin(connectionFactory),rabbitProperties);
        return mqDynamicHandler;
    }


}


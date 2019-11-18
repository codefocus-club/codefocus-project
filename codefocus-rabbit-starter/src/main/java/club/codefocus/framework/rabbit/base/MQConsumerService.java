package club.codefocus.framework.rabbit.base;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
/**
 * @author  jackl
 * @Date: 2019/10/21 16:21
 * @Description:
 */
public interface MQConsumerService extends ChannelAwareMessageListener {

    void setContainer(SimpleMessageListenerContainer container);

    default void shutdown() {}

}

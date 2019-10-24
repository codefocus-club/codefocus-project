package club.codefocus.framework.rabbit.api;

import club.codefocus.framework.rabbit.base.MQConsumerService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import java.io.IOException;

/**
 * @Auther: jackl
 * @Date: 2019/10/21 16:21
 * @Description:
 */
public abstract class AbsMQConsumerService implements MQConsumerService {
    private volatile boolean end = false;
    private SimpleMessageListenerContainer container;
    private boolean autoAck;

    public void setContainer(SimpleMessageListenerContainer container) {
        this.container = container;
        autoAck = container.getAcknowledgeMode().isAutoAck();
    }

    public void shutdown() {
        end = true;
    }

    protected void autoAck(Message message, Channel channel, boolean success) throws IOException {
        if (autoAck) {
            return;
        }

        if (success) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } else {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            autoAck(message, channel, process(message, channel));
        } catch (Exception e) {
            autoAck(message, channel, false);
            throw e;
        } finally {
            if (end) {
                container.stop();
            }
        }
    }
    public void start() {
        container.start();
    }

    public void stop() {
        container.stop();
    }


    public abstract boolean process(Message message, Channel channel);
}

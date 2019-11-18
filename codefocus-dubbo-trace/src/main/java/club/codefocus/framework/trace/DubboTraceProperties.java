package club.codefocus.framework.trace;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author youdw
 */
@ConfigurationProperties("dubbo.trace")
public class DubboTraceProperties {


    private Integer webProduceOrder;


    public Integer getWebProduceOrder() {
        return webProduceOrder;
    }

    public void setWebProduceOrder(Integer webProduceOrder) {
        this.webProduceOrder = webProduceOrder;
    }
}

package club.codefocus.framework.trace.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author youdw
 */
@ConfigurationProperties("dubbo.trace.code-focus")
public class DubboTraceProperties {


    private Integer webProduceOrder;


    public Integer getWebProduceOrder() {
        return webProduceOrder;
    }

    public void setWebProduceOrder(Integer webProduceOrder) {
        this.webProduceOrder = webProduceOrder;
    }


}

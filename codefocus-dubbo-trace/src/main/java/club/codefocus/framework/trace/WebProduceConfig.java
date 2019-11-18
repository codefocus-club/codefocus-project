package club.codefocus.framework.trace;

import club.codefocus.framework.trace.filter.WebProduceTraceIdFilter;
import club.codefocus.framework.trace.properties.DubboTraceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * create at 2019/11/17 9:59 下午
 *
 * @author youdw
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(DubboTraceProperties.class)
public class WebProduceConfig implements WebMvcConfigurer {

    private DubboTraceProperties dubboTraceProperties;

    public WebProduceConfig(DubboTraceProperties dubboTraceProperties) {
        this.dubboTraceProperties = dubboTraceProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        Integer webProduceOrder = dubboTraceProperties.getWebProduceOrder();
        registry.addInterceptor(new WebProduceTraceIdFilter()).order(webProduceOrder == null ? -999 : webProduceOrder);
    }
}

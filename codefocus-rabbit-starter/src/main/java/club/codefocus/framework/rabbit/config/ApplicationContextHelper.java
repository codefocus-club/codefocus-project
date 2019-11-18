package club.codefocus.framework.rabbit.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author  jackl
 * @Date: 2019/10/23 17:51
 * @Description:
 */
@Component
public class ApplicationContextHelper implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    public ApplicationContextHelper() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHelper.applicationContext = applicationContext;
    }

    public Object getBean(String beanName) {
        return applicationContext != null?applicationContext.getBean(beanName):null;
    }
}

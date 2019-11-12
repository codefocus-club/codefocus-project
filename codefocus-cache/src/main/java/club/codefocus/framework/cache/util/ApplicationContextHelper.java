package club.codefocus.framework.cache.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @Auther: jackl
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
    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
}

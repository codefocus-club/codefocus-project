package club.codefocus.framework.trace;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

/**
 * create at 2019/11/18 2:37 下午
 *
 * @author youdw
 */
@Configuration
@EnableConfigurationProperties(TaskExecutionProperties.class)
@Slf4j
@AutoConfigureBefore(TaskExecutionAutoConfiguration.class)
public class MdcTaskExecutionAutoConfiguration {


    @Bean(name = { APPLICATION_TASK_EXECUTOR_BEAN_NAME,
            AsyncAnnotationBeanPostProcessor.DEFAULT_TASK_EXECUTOR_BEAN_NAME })
    @ConfigurationProperties(prefix = "spring.task.execution")
    @ConditionalOnMissingBean(Executor.class)
    public ThreadPoolTaskExecutor taskExecutor() {
        log.debug("mdcThreadPoolTaskExecutor init");
        ThreadPoolTaskExecutor executor = new MdcThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("codefocus-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

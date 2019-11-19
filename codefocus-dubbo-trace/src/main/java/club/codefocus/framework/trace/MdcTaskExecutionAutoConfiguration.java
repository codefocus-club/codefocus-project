package club.codefocus.framework.trace;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * create at 2019/11/18 2:37 下午
 *
 * @author youdw
 */
@Configuration
@EnableConfigurationProperties(TaskExecutionProperties.class)
@Slf4j
public class MdcTaskExecutionAutoConfiguration {


    @Bean
    @ConfigurationProperties(prefix = "spring.task.execution")
    @ConditionalOnBean
    public Executor asyncExecutor() {
        log.info("mdcThreadPoolTaskExecutor init");
        ThreadPoolTaskExecutor executor = new MdcThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("code-focus-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

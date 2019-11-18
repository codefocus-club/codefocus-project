package club.codefocus.framework.trace;

import org.springframework.boot.context.properties.ConfigurationProperties;
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
public class MdcTaskExecutionAutoConfiguration {


    @Bean("codeFoucsAsyncExecutor")
    @ConfigurationProperties(prefix = "spring.task.execution")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new MdcThreadPoolTaskExecutor();
//        executor.setCorePoolSize(corePoolSize);
//        executor.setMaxPoolSize(maxPoolSize);
//        executor.setQueueCapacity(queueCapacity);
//        executor.setThreadNamePrefix("common-provider-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

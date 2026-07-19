package io.github.aimailabs.nexabase.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置。
 * <p>
 * 为操作日志落库提供独立线程池，避免阻塞业务请求。
 * 队列满时由调用线程执行（CallerRunsPolicy），保证日志不丢失。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 操作日志异步线程池。
     */
    @Bean("operLogExecutor")
    public Executor operLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("oper-log-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }
}

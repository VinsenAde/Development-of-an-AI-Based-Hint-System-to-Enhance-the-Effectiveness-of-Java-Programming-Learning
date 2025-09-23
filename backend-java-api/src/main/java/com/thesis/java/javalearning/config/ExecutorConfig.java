package com.thesis.java.javalearning.config; // Sesuaikan dengan package-mu

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
@Configuration
@EnableAsync // Mengaktifkan fitur asinkron Spring
public class ExecutorConfig {

    @Bean(name = "submissionExecutor")
    public Executor submissionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Hanya 2 thread yang aktif
        executor.setMaxPoolSize(4);  // Maksimal 4 thread
        

        // executor.setQueueCapacity(Integer.MAX_VALUE); fail

         executor.setQueueCapacity(10);
        
        executor.setThreadNamePrefix("SubExecutor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
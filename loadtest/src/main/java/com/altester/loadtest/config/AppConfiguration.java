package com.altester.loadtest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AppConfiguration {
    @Bean
    public Executor taskExecutor() {
        return new VirtualThreadTaskExecutor("loadtest-executor");
    }
}

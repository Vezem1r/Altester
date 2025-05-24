package com.altester.loadtest.config;

import com.altester.client.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AppConfiguration {
    @Value("${altester.url}")
    private String URL;

    @Bean
    public Executor taskExecutor() {
        return new VirtualThreadTaskExecutor("loadtest-executor");
    }

    @Bean
    public ApiClient apiClient() {
        ApiClient apiClient = new ApiClient();

        apiClient.setBasePath(URL);

        return apiClient;
    }
}

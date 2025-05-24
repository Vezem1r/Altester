package com.altester.loadtest.config;

import com.altester.client.ApiClient;
import com.altester.client.api.AuthControllerApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApiConfiguration {
    private final ApiClient apiClient;

    @Bean
    public AuthControllerApi authControllerApi() {
        log.info("Auth: {}", apiClient.getAuthentications());
        return new AuthControllerApi(apiClient);
    }
}

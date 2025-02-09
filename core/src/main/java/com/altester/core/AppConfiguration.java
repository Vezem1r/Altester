package com.altester.core;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import lombok.Getter;

@Configuration
@Getter
class AppConfiguration {
    @Value("${AUTH_SERVICE_URL}")
    private String authServiceUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

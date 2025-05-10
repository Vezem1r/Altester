package com.altester.chat_service.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Getter
public class AppConfig {

    @Value("#{'${cors.allowed.origins}'.split(',')}")
    private List<String> allowedOrigins;
}

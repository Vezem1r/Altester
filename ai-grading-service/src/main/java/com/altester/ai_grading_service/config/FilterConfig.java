package com.altester.ai_grading_service.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

  @Bean
  public FilterRegistrationBean<ApiKeyAuthFilter> apiKeyFilterRegistration(
      ApiKeyAuthFilter filter) {
    FilterRegistrationBean<ApiKeyAuthFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(filter);
    registration.addUrlPatterns("/ai/*");
    registration.setName("apiKeyAuthFilter");
    registration.setOrder(1);
    return registration;
  }
}

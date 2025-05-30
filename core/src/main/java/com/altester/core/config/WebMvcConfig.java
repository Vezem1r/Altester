package com.altester.core.config;

import java.nio.file.Paths;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  @Value("${app.upload.question-images:./question-images}")
  private String uploadDir;

  @Value("#{'${cors.allowed.origins}'.split(',')}")
  private List<String> allowedOrigins;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String absolutePath = "file:" + Paths.get(uploadDir).toAbsolutePath().normalize() + "/";
    registry
        .addResourceHandler("/question-images/**")
        .addResourceLocations(absolutePath)
        .setCachePeriod(3600)
        .resourceChain(true)
        .addResolver(new PathResourceResolver());
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/question-images/**")
        .allowedOrigins(allowedOrigins.toArray(new String[0]))
        .allowedMethods("GET", "OPTIONS")
        .allowedHeaders("*");
  }
}

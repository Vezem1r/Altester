package com.altester.core.config;

import com.altester.core.model.auth.enums.RolesEnum;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Value("#{'${cors.allowed.origins}'.split(',')}")
  private List<String> allowedOrigins;

  private final boolean swaggerEnabled = false;

  private final String[] DEMO_WHITE_LIST = {
    "/auth/signin", "/auth/validate-token", "/actuator/**",
  };

  private final AuthenticationProvider authenticationProvider;
  private final JwtAuthFilter jwtAuthFilter;
  private final AuthConfigProperties authConfigProperties;
  private final DemoModeFilter demoModeFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(DEMO_WHITE_LIST)
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasAnyRole(RolesEnum.ADMIN.name())
                    .requestMatchers("/teacher/**")
                    .hasAnyRole(RolesEnum.TEACHER.name(), RolesEnum.ADMIN.name())
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(demoModeFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()));
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(List.of("POST", "GET"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Origin", "Accept"));
    configuration.setAllowCredentials(true);
    configuration.addExposedHeader("*");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/question-images/**", configuration);
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}

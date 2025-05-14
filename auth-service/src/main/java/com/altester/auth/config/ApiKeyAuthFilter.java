package com.altester.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

  @Value("${INTERNAL_API_KEY}")
  private String secretKey;

  private final AntPathMatcher antPathMatcher;

  private static final String API_KEY_HEADER = "x-api-key";
  private static final String[] WHITELIST_PATH = {"/actuator/**"};

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String providedKey = request.getHeader(API_KEY_HEADER);

    if (!isWhitelisted(request.getRequestURI())
        && (providedKey == null || !providedKey.equals(secretKey))) {
      log.warn("Unauthorized access attempt from IP: {}", request.getRemoteAddr());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("Unauthorized: Invalid API Key");
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean isWhitelisted(String path) {
    for (String pattern : WHITELIST_PATH) {
      if (antPathMatcher.match(pattern, path)) {
        return true;
      }
    }
    return false;
  }
}

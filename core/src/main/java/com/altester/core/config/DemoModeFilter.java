package com.altester.core.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class DemoModeFilter extends OncePerRequestFilter {

  private final List<String> allowedPostPaths =
      Arrays.asList("/auth/signin", "/auth/validate-token", "/student/test-attempts/");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String method = request.getMethod();
    String path = request.getServletPath();

    log.debug("Demo filter processing: {} {}", method, path);

    if (HttpMethod.POST.matches(method) && !isAllowedPostPath(path)) {
      log.warn("DEMO MODE: Blocked POST request to {}", path);
      sendDemoError(
          response, "POST requests are disabled", "This is a demo version with read-only access");
      return;
    }

    if (HttpMethod.PUT.matches(method)
        || HttpMethod.DELETE.matches(method)
        || HttpMethod.PATCH.matches(method)) {
      log.warn("DEMO MODE: Blocked {} request to {}", method, path);
      sendDemoError(
          response,
          "Modification requests are disabled",
          "This is a demo version with read-only access");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isAllowedPostPath(String path) {
    return allowedPostPaths.stream().anyMatch(path::startsWith);
  }

  private void sendDemoError(HttpServletResponse response, String error, String message)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String json =
        String.format(
            "{\"error\":\"DEMO MODE: %s\",\"message\":\"%s\",\"demoMode\":true}", error, message);

    response.getWriter().write(json);
  }
}

package com.altester.notification.security;

import com.altester.notification.config.AppConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final AppConfig appConfig;

  private static final String BEARER_PREFIX = "Bearer ";

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String username;

    if (request.getRequestURI().startsWith("/internal/")) {
      String requestApiKey = request.getHeader("x-api-key");

      if (requestApiKey != null && requestApiKey.equals(appConfig.getApiKey())) {
        filterChain.doFilter(request, response);
      } else {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response
            .getWriter()
            .write(
                "{\"status\":\"UNAUTHORIZED\",\"errorCode\":\"INVALID_API_KEY\",\"message\":\"Invalid API key\"}");
        response.setContentType("application/json");
      }
      return;
    }

    if (request.getRequestURI().startsWith("/ws")) {
      filterChain.doFilter(request, response);
      return;
    }

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    jwt = authHeader.substring(BEARER_PREFIX.length());
    try {
      username = jwtService.extractUsername(jwt);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        if (jwtService.isTokenValid(jwt, userDetails)) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
          log.debug("Authenticated user: {}", username);
        }
      }
    } catch (Exception e) {
      log.error("Error validating JWT: {}", e.getMessage());
    }

    filterChain.doFilter(request, response);
  }
}

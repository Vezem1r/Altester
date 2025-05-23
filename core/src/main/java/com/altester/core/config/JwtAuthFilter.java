package com.altester.core.config;

import com.altester.core.exception.JwtAuthenticationException;
import com.altester.core.serviceImpl.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

  private final HandlerExceptionResolver handlerExceptionResolver;
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private static final String BEARER_PREFIX = "Bearer ";

  private final List<String> whitelistedPaths = Arrays.asList("/auth/signin", "/auth/config");

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final String requestPath = request.getServletPath();

    if (shouldSkipAuthentication(requestPath)) {
      filterChain.doFilter(request, response);
      return;
    }

    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String jwt = authHeader.substring(BEARER_PREFIX.length());
      String username = jwtService.extractUsername(jwt);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        if (jwtService.isTokenValid(jwt, userDetails)) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        } else {
          throw JwtAuthenticationException.invalidToken();
        }
      }

      filterChain.doFilter(request, response);
    } catch (ExpiredJwtException e) {
      log.debug("Token expired for request: {}", requestPath);
      handlerExceptionResolver.resolveException(
          request, response, null, JwtAuthenticationException.expiredToken());
    } catch (SignatureException | MalformedJwtException e) {
      log.debug("Invalid token for request: {}", requestPath);
      handlerExceptionResolver.resolveException(
          request, response, null, JwtAuthenticationException.invalidToken());
    } catch (Exception e) {
      log.error("Authentication error: {}", e.getMessage());
      handlerExceptionResolver.resolveException(
          request, response, null, JwtAuthenticationException.malformedToken());
    }
  }

  private boolean shouldSkipAuthentication(String requestPath) {
    return whitelistedPaths.stream()
        .anyMatch(
            path -> {
              if (path.endsWith("/")) {
                return requestPath.startsWith(path);
              } else {
                return requestPath.equals(path);
              }
            });
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return shouldSkipAuthentication(request.getServletPath());
  }
}

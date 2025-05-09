package com.altester.auth.service.serviceImpl;

import com.altester.auth.config.AppConfig;
import com.altester.auth.utils.Constants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final AppConfig appConfig;

  public String generateToken(UserDetails userDetails, String role, boolean rememberMe) {
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("role", role);
    long expirationTime =
        rememberMe
            ? appConfig.getJwtExpiration() * Constants.JET_RME_MULTIPLICATIVE
            : appConfig.getJwtExpiration();
    return generateToken(extraClaims, userDetails, expirationTime);
  }

  private String generateToken(
      Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return buildToken(extraClaims, userDetails, expiration);
  }

  private String buildToken(
      Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(appConfig.getSecretKey());
    return Keys.hmacShaKeyFor(keyBytes);
  }
}

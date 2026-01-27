package com.gcompany.employeemanagement.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import com.gcompany.employeemanagement.model.User;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtil {
    private final Key key;
    private final long accessTokenMs;
    private final String issuer;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.access-expiration-ms}") long accessTokenMs,
                   @Value("${app.jwt.issuer}") String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMs = accessTokenMs;
        this.issuer = issuer;
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTokenMs);

        Map<String, Object> claims = new HashMap<>();

        // User info
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("fullName", user.getFullName());


        // Roles
        List<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getCode())
                .collect(Collectors.toList());
        claims.put("roles", roles);

        // Permissions
        List<String> permissions = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toList());
        claims.put("permissions", permissions);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return Long.parseLong(extractAllClaims(token).get("userId", String.class));
    }

    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }

    public List<String> extractPermissions(String token) {
        return extractAllClaims(token).get("permissions", List.class);
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenValid(String token, User user) {
        final String username = extractUsername(token);
        return (username.equals(user.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}

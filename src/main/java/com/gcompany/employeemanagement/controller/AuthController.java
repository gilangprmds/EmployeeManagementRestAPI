package com.gcompany.employeemanagement.controller;

import com.gcompany.employeemanagement.dto.req.LoginRequest;
import com.gcompany.employeemanagement.dto.resp.AuthResponse;
import com.gcompany.employeemanagement.model.RefreshToken;
import com.gcompany.employeemanagement.model.User;
import com.gcompany.employeemanagement.repository.UserRepository;
import com.gcompany.employeemanagement.security.JwtUtil;
import com.gcompany.employeemanagement.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final long refreshTokenMs;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                          RefreshTokenService refreshTokenService,
                          @Value("${app.jwt.refresh-expiration-ms}") long refreshTokenMs,
                          AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenMs = refreshTokenMs;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req,
            @RequestHeader(value = "X-Client", required = false, defaultValue = "web") String clientType,
            HttpServletResponse response) {

        log.info("Login attempt for user: {}", req.getEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getEmail(),
                        req.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        User user = (User) authentication.getPrincipal();

        // Check if user is active
        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }


        // generate access token
        String accessToken = jwtUtil.generateAccessToken(user);

        // generate and store refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);


        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtUtil.extractExpiration(accessToken).getTime())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .profileImageUrl(user.getProfilePicture())
                .roles(user.getRoleCodes().stream().collect(Collectors.toList()))
                .permissions(user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(auth -> !auth.startsWith("ROLE_"))
                        .collect(Collectors.toList()))
                .build();

        // ============================
        // MODE 1 — MOBILE (no cookie)
        // ============================
        if (clientType.equalsIgnoreCase("mobile")) {
            return ResponseEntity.ok(authResponse);
        }

        // ==================================
        // MODE 2 — WEB (refresh via cookie)
        // ==================================
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true)
                .secure(true) // make true in production
                .path("/")
                .maxAge(refreshTokenMs / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        authResponse.setRefreshToken(null);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(
            @RequestHeader(value = "X-Client", required = false, defaultValue = "web") String clientType,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshTokenValue;

        // MOBILE: refresh token dikirim di body
        if (clientType.equalsIgnoreCase("mobile")) {
            if (body == null || !body.containsKey("refreshToken")) {
                return ResponseEntity.status(401).body(Map.of("error", "Refresh token missing"));
            }
            refreshTokenValue = body.get("refreshToken");
        }
        // WEB: refresh token ambil dari cookie
        else {
            Cookie cookie = WebUtils.getCookie(request, "refreshToken");
            if (cookie == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No refresh token cookie"));
            }
            refreshTokenValue = cookie.getValue();
        }

        // lookup refresh token
        Optional<RefreshToken> opt = refreshTokenService.findByToken(refreshTokenValue);
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
        }


        RefreshToken rt = opt.get();
        User user = rt.getUser();

        // Check if user is active
        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }


        if (rt.getExpiry().isBefore(Instant.now())) {
            refreshTokenService.deleteByToken(rt.getToken());

            if (!clientType.equalsIgnoreCase("mobile")) {
                ResponseCookie delete = ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .path("/")
                        .maxAge(0)
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, delete.toString());
            }

            return ResponseEntity.status(401).body(Map.of("error", "Refresh token expired"));
        }

        String newAccess = jwtUtil.generateAccessToken(rt.getUser());

        // Prepare response
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(newAccess)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.extractExpiration(newAccess).getTime())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .profileImageUrl(user.getProfilePicture())
                .roles(user.getRoleCodes().stream().collect(Collectors.toList()))
                .permissions(user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(auth -> !auth.startsWith("ROLE_"))
                        .collect(Collectors.toList()))
                .build();

        log.info("Token refreshed for user: {}", user.getUsername());

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "X-Client", required = false, defaultValue = "web") String clientType,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshTokenValue;

        // mobile — token via body
        if (clientType.equalsIgnoreCase("mobile")) {
            if (body == null || !body.containsKey("refreshToken")) {
                return ResponseEntity.ok(Map.of("ok", true));
            }
            refreshTokenValue = body.get("refreshToken");
        }
        // web — token via cookie
        else {
            Cookie cookie = WebUtils.getCookie(request, "refreshToken");
            if (cookie == null) {
                return ResponseEntity.ok(Map.of("ok", true));
            }
            refreshTokenValue = cookie.getValue();

            // delete cookie
            ResponseCookie delete = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .path("/")
                    .maxAge(0)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, delete.toString());
        }

        refreshTokenService.deleteByToken(refreshTokenValue);

        return ResponseEntity.ok(Map.of("ok", true));
    }
}

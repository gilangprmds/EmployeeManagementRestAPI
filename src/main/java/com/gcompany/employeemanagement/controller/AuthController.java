package com.gcompany.employeemanagement.controller;

import com.gcompany.employeemanagement.dto.req.LoginRequest;
import com.gcompany.employeemanagement.model.RefreshToken;
import com.gcompany.employeemanagement.model.Role;
import com.gcompany.employeemanagement.model.User;
import com.gcompany.employeemanagement.repository.UserRepository;
import com.gcompany.employeemanagement.security.JwtUtil;
import com.gcompany.employeemanagement.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final long refreshTokenMs;

    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                          RefreshTokenService refreshTokenService,
                          @org.springframework.beans.factory.annotation.Value("${app.jwt.refresh-expiration-ms}") long refreshTokenMs) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenMs = refreshTokenMs;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        Optional<User> opt = userRepo.findByUsername(req.getUsername());
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        User user = opt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        // generate access token
        String accessToken = jwtUtil.generateAccessToken(user);
        // generate refresh token (persist)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // set refresh token as HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true)
                .secure(false) // set true in production with https
                .path("/")
                .maxAge(refreshTokenMs / 1000)
                .sameSite("Lax")
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // prepare user payload for frontend (username + roles)
        Map<String, Object> userMap = Map.of(
                "username", user.getUsername(),
                "roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList())
        );

        // return both accessToken and user
        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "user", userMap
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, "refreshToken");
        if (cookie == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No refresh token"));
        }
        String token = cookie.getValue();
        Optional<RefreshToken> opt = refreshTokenService.findByToken(token);
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
        }
        RefreshToken rt = opt.get();
        if (rt.getExpiry().isBefore(Instant.now())) {
            // expired -> delete and reject
            refreshTokenService.deleteByToken(rt.getToken());
            // clear cookie
            ResponseCookie delete = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .path("/")
                    .maxAge(0)
                    .build();
            response.setHeader(HttpHeaders.SET_COOKIE, delete.toString());
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token expired"));
        }

        // generate new access token
        String newAccess = jwtUtil.generateAccessToken(rt.getUser());

        // prepare user payload for frontend
        User user = rt.getUser();
        Map<String, Object> userMap = Map.of(
                "username", user.getUsername(),
                "roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList())
        );

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccess,
                "user", userMap
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, "refreshToken");
        if (cookie != null) {
            refreshTokenService.deleteByToken(cookie.getValue());
            ResponseCookie delete = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .path("/")
                    .maxAge(0)
                    .build();
            response.setHeader(HttpHeaders.SET_COOKIE, delete.toString());
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }
}

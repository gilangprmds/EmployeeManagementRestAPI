package com.gcompany.employeemanagement.controller;

import com.gcompany.employeemanagement.dto.req.LoginRequest;
import com.gcompany.employeemanagement.dto.resp.UserResponse;
import com.gcompany.employeemanagement.model.RefreshToken;
import com.gcompany.employeemanagement.model.User;
import com.gcompany.employeemanagement.repository.UserRepository;
import com.gcompany.employeemanagement.security.JwtUtil;
import com.gcompany.employeemanagement.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
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
                          @Value("${app.jwt.refresh-expiration-ms}") long refreshTokenMs) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenMs = refreshTokenMs;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req,
            @RequestHeader(value = "X-Client", required = false, defaultValue = "web") String clientType,
            HttpServletResponse response) {

        Optional<User> opt = userRepo.findByEmail(req.getEmail());
        if (opt.isEmpty() || !passwordEncoder.matches(req.getPassword(), opt.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        User user = opt.get();

        // generate access token
        String accessToken = jwtUtil.generateAccessToken(user);

        // generate and store refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        Map<String, Object> userMap = Map.of(
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "role", user.getRole().name()
        );

        // ============================
        // MODE 1 — MOBILE (no cookie)
        // ============================
        if (clientType.equalsIgnoreCase("mobile")) {
            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken.getToken(),
                    "user", userMap
            ));
        }

        // ==================================
        // MODE 2 — WEB (refresh via cookie)
        // ==================================
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true)
                .secure(false) // make true in production
                .path("/")
                .maxAge(refreshTokenMs / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "user", userMap
        ));
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
        UserResponse userResponse = UserResponse.builder()
                .email(rt.getUser().getEmail())
                .fullName(rt.getUser().getFullName())
                .role(rt.getUser().getRole().name())
                .build();

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccess,
                "user", userResponse
        ));
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

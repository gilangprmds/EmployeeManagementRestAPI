package com.gcompany.employeemanagement.service;

import com.gcompany.employeemanagement.model.RefreshToken;
import com.gcompany.employeemanagement.model.User;
import com.gcompany.employeemanagement.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final long refreshTokenMs;

    public RefreshTokenService(RefreshTokenRepository repo, @Value("${app.jwt.refresh-expiration-ms}") long refreshTokenMs) {
        this.repo = repo;
        this.refreshTokenMs = refreshTokenMs;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiry(Instant.now().plusMillis(refreshTokenMs));
        repo.save(token);
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return repo.findByToken(token);
    }

    @Transactional
    public void deleteByToken(String token) {
        repo.deleteByToken(token);
    }
}

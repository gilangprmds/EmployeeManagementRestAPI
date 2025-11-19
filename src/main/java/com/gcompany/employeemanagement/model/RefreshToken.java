package com.gcompany.employeemanagement.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id @GeneratedValue
    private Long id;
    private String token;
    private Instant expiry;
    @ManyToOne
    private User user;
    private Instant createdAt = Instant.now();

}


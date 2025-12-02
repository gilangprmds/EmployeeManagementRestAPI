package com.gcompany.employeemanagement.model;

import com.gcompany.employeemanagement.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "attendances")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relasi ke User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // tanggal kerja (bukan datetime)
    @Column(nullable = false)
    private LocalDate date;

    // CHECK-IN
    private OffsetDateTime checkinTime;

    private Double checkinLat;
    private Double checkinLng;

    private String checkinPhoto;

    // CHECK-OUT
    private OffsetDateTime checkoutTime;

    private Double checkoutLat;
    private Double checkoutLng;

    private String checkoutPhoto;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    private String note;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

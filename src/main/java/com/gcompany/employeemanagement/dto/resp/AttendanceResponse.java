package com.gcompany.employeemanagement.dto.resp;

import com.gcompany.employeemanagement.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
public class AttendanceResponse {

    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userRole;
    private String userProfileImageUrl;

    private LocalDate date;

    private OffsetDateTime checkinTime;
    private Double checkinLat;
    private Double checkinLng;
    private String checkinPhoto;

    private OffsetDateTime checkoutTime;
    private Double checkoutLat;
    private Double checkoutLng;
    private String checkoutPhoto;

    private AttendanceStatus status;
    private String note;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}


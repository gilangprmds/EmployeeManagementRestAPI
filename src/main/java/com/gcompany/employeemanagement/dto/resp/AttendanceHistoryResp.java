package com.gcompany.employeemanagement.dto.resp;

import com.gcompany.employeemanagement.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
public class AttendanceHistoryResp {
    private LocalDate date;
    private OffsetDateTime checkinTime;
    private OffsetDateTime checkoutTime;
    private AttendanceStatus status;
}

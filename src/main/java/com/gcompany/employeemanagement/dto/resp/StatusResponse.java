package com.gcompany.employeemanagement.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatusResponse {
    private OffsetDateTime checkInTime;
    private OffsetDateTime checkOutTime;
}

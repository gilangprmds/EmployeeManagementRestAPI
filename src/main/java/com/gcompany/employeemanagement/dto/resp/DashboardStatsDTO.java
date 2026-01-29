package com.gcompany.employeemanagement.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private  Long totalEmployees;
    private  Long activeToday;
    private  Long averageAttendance;
}

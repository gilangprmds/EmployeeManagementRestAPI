package com.gcompany.employeemanagement.controller;

import com.gcompany.employeemanagement.dto.resp.DashboardStatsDTO;
import com.gcompany.employeemanagement.service.AttendanceService;
import com.gcompany.employeemanagement.service.UserService2;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Stats", description = "APIs for dashboard stats")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {
    private final UserService2 userService;
    private final AttendanceService  attendanceService;
//    private final AttendanceService attendanceService;
//    private final LeaveService leaveService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = DashboardStatsDTO.builder()
                .totalEmployees(userService.getTotalUsersCount())
                .activeToday(attendanceService.getCountAttendanceToday())
//                .avgAttendance(attendanceService.getAverageAttendanceRate())
//                .departmentDistribution(userService.getDepartmentDistribution())
                .build();

        return ResponseEntity.ok(stats);
    }

//    @GetMapping("/attendance")
//    public ResponseEntity<List<MonthlyAttendanceDTO>> getAttendanceData(
//            @RequestParam(defaultValue = "2024") Integer year) {
//        List<MonthlyAttendanceDTO> data = attendanceService.getMonthlyAttendance(year);
//        return ResponseEntity.ok(data);
//    }
//
//    @GetMapping("/leave-distribution")
//    public ResponseEntity<List<LeaveTypeDistributionDTO>> getLeaveDistribution() {
//        List<LeaveTypeDistributionDTO> distribution = leaveService.getLeaveTypeDistribution();
//        return ResponseEntity.ok(distribution);
//    }
}

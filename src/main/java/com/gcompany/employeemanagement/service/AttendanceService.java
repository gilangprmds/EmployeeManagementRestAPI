package com.gcompany.employeemanagement.service;

import com.gcompany.employeemanagement.dto.req.AttendanceRequest;
import com.gcompany.employeemanagement.dto.resp.AttendanceResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AttendanceService {

    ResponseEntity<?> checkIn(AttendanceRequest request);

    ResponseEntity<?> checkOut(AttendanceRequest request);

    ResponseEntity<?> getHistoryForUser();

    ResponseEntity<?> getTodayStatus();

    ResponseEntity<?> getAllAttendance();
}

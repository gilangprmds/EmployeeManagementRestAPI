package com.gcompany.employeemanagement.controller;

import com.gcompany.employeemanagement.dto.req.AttendanceRequest;
import com.gcompany.employeemanagement.dto.resp.AttendanceResponse;
import com.gcompany.employeemanagement.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    /** --------------------------
     *        CHECK IN
     * ------------------------- */
    @PostMapping(
            value = "/checkin",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> checkIn(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        AttendanceRequest req = new AttendanceRequest();
        req.setLatitude(latitude);
        req.setLongitude(longitude);
        req.setPhoto(image);

        return attendanceService.checkIn(req);
    }

    /** --------------------------
     *        CHECK OUT
     * ------------------------- */
    @PostMapping(
            value = "/checkout",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> checkOut(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        AttendanceRequest req = new AttendanceRequest();
        req.setLatitude(latitude);
        req.setLongitude(longitude);
        req.setPhoto(image);

        return attendanceService.checkOut(req);
    }

    /** --------------------------
     *  GET HISTORY By USER
     * ------------------------- */
    @GetMapping("/history")
    public ResponseEntity<?> getHistoryByUser() {
        return attendanceService.getHistoryForUser();
    }

    /** --------------------------
     *  GET TODAY STATUS
     * ------------------------- */
    @GetMapping("/today")
    public ResponseEntity<?> getTodayStatus() {
        return attendanceService.getTodayStatus();
    }

    /** --------------------------
     *  GET ALL ATTENDANCE
     * ------------------------- */
    @GetMapping
    public ResponseEntity<?> getAllAttendance() {
        return attendanceService.getAllAttendance();
    }
}

package com.gcompany.employeemanagement.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gcompany.employeemanagement.dto.Response;
import com.gcompany.employeemanagement.dto.req.AttendanceRequest;
import com.gcompany.employeemanagement.dto.resp.AttendanceHistoryResp;
import com.gcompany.employeemanagement.dto.resp.AttendanceResponse;
import com.gcompany.employeemanagement.dto.resp.StatusResponse;
import com.gcompany.employeemanagement.enums.AttendanceStatus;
import com.gcompany.employeemanagement.enums.Role;
import com.gcompany.employeemanagement.model.Attendance;
import com.gcompany.employeemanagement.model.User;
import com.gcompany.employeemanagement.repository.AttendanceRepository;
import com.gcompany.employeemanagement.repository.UserRepository;
import com.gcompany.employeemanagement.service.AttendanceService;
import com.gcompany.employeemanagement.utils.AttendanceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepo;
    private final UserRepository userRepo;
    private final AttendanceMapper attendanceMapper;
    private final String uploadDir = "uploads/";
    @Autowired
    private Cloudinary cloudinary;

    // contoh jam kerja normal (08:00)
    private final LocalTime officeStartTime = LocalTime.of(8, 0);


    /**
     * --------------------------
     * CHECK IN
     * -------------------------
     */
    @Override
    public ResponseEntity<?> checkIn(AttendanceRequest request) {
        Response<AttendanceResponse> response = new Response<>();
        try {
            Long userId = getCurrentUserId();
            Optional<User> user = userRepo.findById(userId);
            if (user.isEmpty()) {
                response.setMessage("User ID " + userId + "  does not exist");
                log.error("User ID " + userId + "  does not exist");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }

            LocalDate today = LocalDate.now();

            // Cek unfinished attendance
            boolean hasUnfinished = attendanceRepo
                    .existsByUser_IdAndDateAndCheckoutTimeIsNull(userId, today);

            if (hasUnfinished) {
                response.setMessage("Anda sudah check-in dan belum checkout.");
                log.error("Anda sudah check-in dan belum checkout.");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }

            Attendance attendance = new Attendance();
            attendance.setUser(user.get());
            attendance.setDate(today);
            attendance.setCheckinTime(OffsetDateTime.now());
            attendance.setCheckinLat(request.getLatitude());
            attendance.setCheckinLng(request.getLongitude());

//        /** ---- SIMPAN FOTO CHECKIN ---- */
//        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
//            String fileName = saveFile(request.getPhoto());
//            attendance.setCheckinPhoto(fileName);
//        }

            /** ---- SIMPAN FOTO CHECKIN Ke Cloudinary ---- */
            if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
                String urlImage = uploadFileCloudinary(request.getPhoto());
                attendance.setCheckinPhoto(urlImage);
            }

            // status
            if (LocalTime.now().isAfter(officeStartTime)) {
                attendance.setStatus(AttendanceStatus.LATE);
            } else {
                attendance.setStatus(AttendanceStatus.PRESENT);
            }

            attendance.setCreatedAt(OffsetDateTime.now());
            attendanceRepo.save(attendance);
            AttendanceResponse attendanceResponse = attendanceMapper.toDTO(attendance);

            response.setData(attendanceResponse);
            response.setMessage("User ID " + userId + "  has been checked in successfully");
            log.info("User ID " + userId + "  has been checked in successfully");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }


    /**
     * --------------------------
     * CHECK OUT
     * -------------------------
     */
    @Override
    public ResponseEntity<?> checkOut(AttendanceRequest request) {
        Response<AttendanceResponse> response = new Response<>();
        try {
            Long userId = getCurrentUserId();
            LocalDate today = LocalDate.now();

            Optional<Attendance> attendance = attendanceRepo.findFirstByUser_IdAndDateAndCheckoutTimeIsNull(userId, today);

            if (attendance.isEmpty()) {
                response.setMessage("Anda belum check-in hari ini.");
                log.error("Anda belum check-in hari ini.");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }

            Attendance attendance1 = attendance.get();
            attendance1.setCheckoutTime(OffsetDateTime.now());
            attendance1.setCheckoutLat(request.getLatitude());
            attendance1.setCheckoutLng(request.getLongitude());

//        /** ---- SIMPAN FOTO CHECKOUT ---- */
//        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
//            String fileName = saveFile(request.getPhoto());
//            attendance.setCheckoutPhoto(fileName);
//        }

            /** ---- SIMPAN FOTO Ke Cloudinary ---- */
            if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
                String urlImage = uploadFileCloudinary(request.getPhoto());
                attendance1.setCheckoutPhoto(urlImage);
            }

            attendance1.setUpdatedAt(OffsetDateTime.now());
            attendanceRepo.save(attendance1);
            AttendanceResponse attendanceResponse = attendanceMapper.toDTO(attendance1);
            response.setData(attendanceResponse);
            response.setMessage("User ID " + userId + "  has been checked out successfully");
            log.info("User ID " + userId + "  has been checked out successfully");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }


    /**
     * --------------------------
     * GET HISTORY PER USER
     * -------------------------
     */
    @Override
    public ResponseEntity<?> getHistoryForUser() {
        Response<Object> response = new Response<>();
        Long userId = getCurrentUserId();
        try {
            List<Attendance> attendanceList = attendanceRepo.findByUser_IdOrderByDateDesc(userId);
            List<AttendanceHistoryResp>  attendanceHistoryRespList = attendanceList
                    .stream().map(attendance -> AttendanceHistoryResp.builder()
                            .date(attendance.getDate())
                            .checkinTime(attendance.getCheckinTime())
                            .checkoutTime(attendance.getCheckoutTime())
                            .status(attendance.getStatus())
                            .build()
                    )
                    .toList();

            response.setData(attendanceHistoryRespList);
            response.setMessage("History for User ID " + userId + "  has been got successfully");
            log.info("History for User ID " + userId + "  has been got successfully");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }

    /**
     * --------------------------
     * GET TODAY STATUS
     * -------------------------
     */
    @Override
    public ResponseEntity<?> getTodayStatus() {
        Long userId = getCurrentUserId();
        Response<Object> response = new Response<>();
        try {
            LocalDate today = LocalDate.now();
            Optional<Attendance> attendance = attendanceRepo.findFirstByUser_IdAndDate(userId, today);
            if (attendance.isEmpty() ) {
                StatusResponse statusResponse = StatusResponse.builder()
                        .checkInTime(null)
                        .checkOutTime(null)
                        .build();

                response.setData(statusResponse);
                response.setMessage("User ID " + userId + " has not any record today");
                log.info("User ID " + userId + " has not any record today");
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }
            Attendance attendance1 = attendance.get();
            StatusResponse statusResponse = StatusResponse.builder()
                    .checkInTime(attendance1.getCheckinTime())
                    .checkOutTime(attendance1.getCheckoutTime())
                    .build();


            response.setData(statusResponse);
            response.setMessage("Today status for User ID " + userId + "  has been got successfully");
            log.info("Today status for User ID " + userId + "  has been got successfully");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }


    /**
     * --------------------------
     * GET ALL ATTENDANCE
     * -------------------------
     */
    @Override
    public ResponseEntity<?> getAllAttendance(
            Pageable pageable,
            LocalDate date,
            String name,
            String status,
            String role,
            LocalDate startDate,
            LocalDate endDate) {
        Response<Object> response = new Response<>();
        try {
            Specification<Attendance> spec = Specification.where(null);

            // 1. Filter Rentang Tanggal
            if (startDate != null) {
                spec = spec.and((root, query, cb) ->
                        cb.greaterThanOrEqualTo(root.get("date"), startDate)
                );
            }

            if (endDate != null) {
                spec = spec.and((root, query, cb) ->
                        cb.lessThanOrEqualTo(root.get("date"), endDate)
                );
            }

            if (name != null && !name.isBlank()) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("user").get("fullName")), "%" + name.toLowerCase() + "%")
                );
            }

            Role role1;
            if (role != null && !role.isBlank()) {
                try {
                    role1 = Role.valueOf(role.toUpperCase());
                } catch (IllegalArgumentException e) {
                    response.setMessage("Attendance User With Role " + role + " not found");
                    log.error("Attendance User With Role" + role + " not found");
                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(response);
                }
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("user").get("role"),  role1)
                );
            }

            AttendanceStatus attendanceStatus;
            if (status != null && !status.isBlank()) {
                try {
                    attendanceStatus = AttendanceStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    response.setMessage("Attendance With Status " + status + " not found");
                    log.error("Attendance With Status" + status + " not found");
                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(response);
                }
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("status"),  attendanceStatus)
                );
            }

            Page<Attendance> attendancesPage = attendanceRepo.findAll(spec, pageable);
            List<Attendance> attendanceList = attendancesPage.getContent();

            List<AttendanceResponse> attendanceResponses = attendanceList.stream()
                    .map(attendanceMapper::toDTO).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("attendances", attendanceResponses);
            result.put("currentPage", attendancesPage.getNumber());
            result.put("currentItem", attendancesPage.getNumberOfElements());
            result.put("totalItems", attendancesPage.getTotalElements());
            result.put("totalPages", attendancesPage.getTotalPages());

            response.setMessage("Attendances retrieved successfully");
            response.setData(result);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }

    /**
     * --------------------------
     * GET CURRENT USER FROM JWT
     * -------------------------
     */
    private Long getCurrentUserId() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getId();
    }

    public String saveFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            // create folder if not exists
            File folder = new File(uploadDir);
            if (!folder.exists()) folder.mkdirs();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);

            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    public String uploadFileCloudinary(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("url").toString(); // Ambil URL gambar yang di-upload
        } catch (IOException e) {
            throw new RuntimeException("Gagal mengunggah gambar ke Cloudinary", e);
        }
    }
}


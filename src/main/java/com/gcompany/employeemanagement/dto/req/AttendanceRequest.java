package com.gcompany.employeemanagement.dto.req;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AttendanceRequest {
    private Double latitude;
    private Double longitude;
    private MultipartFile photo;  // Ubah ke MultipartFile
}

package com.gcompany.employeemanagement.mapper;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gcompany.employeemanagement.dto.req.UserUpdateRequest;
import com.gcompany.employeemanagement.dto.resp.UserDetailResponse;
import com.gcompany.employeemanagement.model.User;
import com.gcompany.employeemanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final RoleRepository roleRepository;

    @Autowired
    private final Cloudinary  cloudinary;

    public UserDetailResponse toUserDetailResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfilePicture())
                .status(user.getStatus())
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .enabled(user.isEnabled())
                .roles(user.getRoleCodes().stream().collect(Collectors.toList()))
                .permissions(user.getPermissionNames().stream().collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }

    public void updateUserFromRequest(User user, UserUpdateRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
            if (user.getLastName() != null) {
                user.setFullName(request.getFirstName().concat(" ").concat(user.getLastName()));
            }
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
            user.setFullName(user.getFirstName().concat(" ").concat(request.getLastName()));
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfilePicture() != null) {
            String urlPicture = uploadFileCloudinary(request.getProfilePicture());
            user.setProfilePicture(urlPicture);
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getAccountNonExpired() != null) {
            user.setAccountNonExpired(request.getAccountNonExpired());
        }
        if (request.getAccountNonLocked() != null) {
            user.setAccountNonLocked(request.getAccountNonLocked());
        }
        if (request.getCredentialsNonExpired() != null) {
            user.setCredentialsNonExpired(request.getCredentialsNonExpired());
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
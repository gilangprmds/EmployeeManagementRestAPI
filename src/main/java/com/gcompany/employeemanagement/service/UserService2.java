package com.gcompany.employeemanagement.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gcompany.employeemanagement.dto.req.UserCreateRequest;
import com.gcompany.employeemanagement.dto.req.UserRoleAssignRequest;
import com.gcompany.employeemanagement.dto.req.UserUpdateRequest;
import com.gcompany.employeemanagement.dto.resp.PaginatedResponse;
import com.gcompany.employeemanagement.dto.resp.UserDetailResponse;
import com.gcompany.employeemanagement.enums.UserStatus;
import com.gcompany.employeemanagement.exception.BusinessRuleException;
import com.gcompany.employeemanagement.exception.ResourceNotFoundException;
import com.gcompany.employeemanagement.mapper.UserMapper;
import com.gcompany.employeemanagement.model.Role;
import com.gcompany.employeemanagement.model.User;
import com.gcompany.employeemanagement.repository.RoleRepository;
import com.gcompany.employeemanagement.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService2 {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SecurityService securityService;

    @Autowired
    private final Cloudinary cloudinary;

    // ========== CRUD Operations ==========

    @PreAuthorize("hasRole('ADMIN')")
    public UserDetailResponse createUser(UserCreateRequest request) {
        log.info("Creating user with email: {}", request.getEmail());


        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already exists: " + request.getEmail());
        }

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .fullName(request.getFirstName().concat(" ").concat(request.getLastName()))
                .phoneNumber(request.getPhoneNumber())
                .status(UserStatus.ACTIVE)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        // Assign roles if provided
        if (request.getRoleCodes() != null && !request.getRoleCodes().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findByCodes(request.getRoleCodes()));
            user.setRoles(roles);
        } else {
            // Assign default role if no roles specified
            roleRepository.findByDefaultRole(true)
                    .stream()
                    .findFirst()
                    .ifPresent(defaultRole -> user.setRoles(Set.of(defaultRole)));
        }

        // assign profilepicture if provided

        if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
            String urlImage = uploadFileCloudinary(request.getProfilePicture());
            user.setProfilePicture(urlImage);
        }

        // Set created by
        User currentUser = securityService.getCurrentUser();
        if (currentUser != null) {
            user.setCreatedBy(currentUser.getId());
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getUsername());

        return userMapper.toUserDetailResponse(savedUser);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDetailResponse getUserById(Long userId) {
        log.info("Fetching user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return userMapper.toUserDetailResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaginatedResponse<UserDetailResponse> getAllUsers(
            int page, int size, String sortBy, String sortDir, String name, String role, String status) {
        log.info("Fetching all users - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<User> spec = Specification.where(null);

        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("fullName")), "%" + name.toLowerCase() + "%")
            );
        }

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("status")), status.toLowerCase())
            );
        }

        if (role != null && !role.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                // 1. Lakukan Join dari entitas User ke entitas Role
                // Ganti "roles" dengan nama field di User entity Anda
                Join<Object, Object> rolesJoin = root.join("roles");

                // 2. Pastikan hasil query unik (distinct) agar user tidak muncul dobel
                // jika dia punya banyak role yang mirip
                query.distinct(true);

                // 3. Bandingkan parameter dengan field "name" di entitas Role
                return cb.equal(cb.lower(rolesJoin.get("code")), role.toLowerCase());
            });
        }

        Page<User> usersPage = userRepository.findAll(spec, pageable);


        List<UserDetailResponse> userResponses = usersPage.getContent().stream()
                .map(userMapper::toUserDetailResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.<UserDetailResponse>builder()
                .content(userResponses)
                .page(usersPage.getNumber())
                .size(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .last(usersPage.isLast())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDetailResponse updateUser(Long userId, UserUpdateRequest request) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check email uniqueness if changing email
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessRuleException("Email already exists: " + request.getEmail());
            }
        }

        // Update user fields
        userMapper.updateUserFromRequest(user, request);

        // Update roles if provided
        if (request.getRoleCodes() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findByCodes(request.getRoleCodes()));
            user.setRoles(roles);
        }

        // Set updated by
        User currentUser = securityService.getCurrentUser();
        if (currentUser != null) {
            user.setUpdatedBy(currentUser.getId());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getUsername());

        return userMapper.toUserDetailResponse(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Prevent deleting self
        User currentUser = securityService.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(userId)) {
            throw new BusinessRuleException("Cannot delete your own account");
        }

        // Prevent deleting system users (like admin)
        if (user.hasRole("ADMIN") && userRepository.findByRoleCode("ADMIN").size() <= 1) {
            throw new BusinessRuleException("Cannot delete the last admin user");
        }

        userRepository.delete(user);
        log.info("User deleted successfully: {}", userId);
    }

    // ========== Business Operations ==========

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDetailResponse updateUserRoles(Long userId, UserRoleAssignRequest request) {
        log.info("Updating roles for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> roles = new HashSet<>(roleRepository.findByCodes(request.getRoleCodes()));
        user.setRoles(roles);

        // Set updated by
        User currentUser = securityService.getCurrentUser();
        if (currentUser != null) {
            user.setUpdatedBy(currentUser.getId());
        }

        User updatedUser = userRepository.save(user);
        log.info("Roles updated for user: {}", updatedUser.getUsername());

        return userMapper.toUserDetailResponse(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDetailResponse activateUser(Long userId) {
        log.info("Activating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.activate();

        User updatedUser = userRepository.save(user);
        log.info("User activated: {}", updatedUser.getUsername());

        return userMapper.toUserDetailResponse(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDetailResponse deactivateUser(Long userId) {
        log.info("Deactivating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Prevent deactivating self
        User currentUser = securityService.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(userId)) {
            throw new BusinessRuleException("Cannot deactivate your own account");
        }

        user.deactivate();

        User updatedUser = userRepository.save(user);
        log.info("User deactivated: {}", updatedUser.getUsername());

        return userMapper.toUserDetailResponse(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDetailResponse lockUser(Long userId) {
        log.info("Locking user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Prevent locking self
        User currentUser = securityService.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(userId)) {
            throw new BusinessRuleException("Cannot lock your own account");
        }

        user.lock();

        User updatedUser = userRepository.save(user);
        log.info("User locked: {}", updatedUser.getUsername());

        return userMapper.toUserDetailResponse(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDetailResponse unlockUser(Long userId) {
        log.info("Unlocking user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.unlock();

        User updatedUser = userRepository.save(user);
        log.info("User unlocked: {}", updatedUser.getUsername());

        return userMapper.toUserDetailResponse(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDetailResponse changeUserPassword(Long userId, String newPassword) {
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate password strength
        if (newPassword.length() < 8) {
            throw new BusinessRuleException("Password must be at least 8 characters long");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        User updatedUser = userRepository.save(user);
        log.info("Password changed for user: {}", updatedUser.getUsername());

        return userMapper.toUserDetailResponse(updatedUser);
    }

    // ========== Search Operations ==========

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public List<UserDetailResponse> searchUsers(String keyword) {
        log.info("Searching users with keyword: {}", keyword);

        List<User> users = userRepository.findByFullNameContainingIgnoreCase(keyword);

        return users.stream()
                .map(userMapper::toUserDetailResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDetailResponse> getUsersByRole(String roleCode) {
        log.info("Fetching users with role: {}", roleCode);

        List<User> users = userRepository.findByRoleCode(roleCode);

        return users.stream()
                .map(userMapper::toUserDetailResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public List<UserDetailResponse> getActiveUsers() {
        log.info("Fetching all active users");

        List<User> users = userRepository.findByStatus(UserStatus.ACTIVE);

        return users.stream()
                .map(userMapper::toUserDetailResponse)
                .collect(Collectors.toList());
    }

    // ========== Helper Methods ==========

    public User getCurrentUserEntity() {
        return securityService.getCurrentUser();
    }

    public boolean isCurrentUser(Long userId) {
        return securityService.isSelf(userId);
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
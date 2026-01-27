package com.gcompany.employeemanagement.config;

import com.gcompany.employeemanagement.enums.ActionType;
import com.gcompany.employeemanagement.enums.ResourceType;
import com.gcompany.employeemanagement.enums.UserStatus;
import com.gcompany.employeemanagement.model.Permission;
import com.gcompany.employeemanagement.model.Role;
import com.gcompany.employeemanagement.model.User;
import com.gcompany.employeemanagement.repository.PermissionRepository;
import com.gcompany.employeemanagement.repository.RoleRepository;
import com.gcompany.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    public CommandLineRunner seedData() {
        return args -> {
            try {
                log.info("Starting data seeding...");

                // 1. Seed Permissions
                if (permissionRepository.count() == 0) {
                    seedPermissions();
                    log.info("Permissions seeded successfully");
                } else {
                    log.info("Permissions already exist, count: {}", permissionRepository.count());
                }

                // 2. Seed Roles
                if (roleRepository.count() == 0) {
                    seedRoles();
                    log.info("Roles seeded successfully");
                } else {
                    log.info("Roles already exist, count: {}", roleRepository.count());
                }

                // 3. Seed Admin User
                if (userRepository.count() == 0) {
                    seedAdminUser();
                    log.info("Admin user seeded successfully");
                } else {
                    log.info("Users already exist, count: {}", userRepository.count());
                }

                log.info("Data seeding completed!");
            } catch (Exception e) {
                log.error("Data seeding failed: ", e);
                throw e; // Rollback transaction
            }
        };
    }

    private void seedPermissions() {
        // List semua permissions dengan format yang konsisten
        List<Permission> permissions = Arrays.asList(
                // User permissions
                Permission.of(ResourceType.USER, ActionType.CREATE, "ALL"),
                Permission.of(ResourceType.USER, ActionType.READ, "ALL"),
                Permission.of(ResourceType.USER, ActionType.UPDATE, "ALL"),
                Permission.of(ResourceType.USER, ActionType.DELETE, "ALL"),

                // Employee permissions
                Permission.of(ResourceType.EMPLOYEE, ActionType.CREATE, "ALL"),
                Permission.of(ResourceType.EMPLOYEE, ActionType.READ, "ALL"),
                Permission.of(ResourceType.EMPLOYEE, ActionType.UPDATE, "ALL"),
                Permission.of(ResourceType.EMPLOYEE, ActionType.DELETE, "ALL"),
                Permission.of(ResourceType.EMPLOYEE, ActionType.READ, "SELF"),
                Permission.of(ResourceType.EMPLOYEE, ActionType.READ, "TEAM"),
                Permission.of(ResourceType.EMPLOYEE, ActionType.READ, "DEPARTMENT"),

                // Salary permissions
                Permission.of(ResourceType.SALARY, ActionType.READ, "ALL"),
                Permission.of(ResourceType.SALARY, ActionType.UPDATE, "ALL"),
                Permission.of(ResourceType.SALARY, ActionType.READ, "SELF"),

                // Leave permissions
                Permission.of(ResourceType.LEAVE_REQUEST, ActionType.CREATE, "ALL"),
                Permission.of(ResourceType.LEAVE_REQUEST, ActionType.READ, "ALL"),
                Permission.of(ResourceType.LEAVE_REQUEST, ActionType.APPROVE, "ALL"),
                Permission.of(ResourceType.LEAVE_REQUEST, ActionType.REJECT, "ALL"),

                // Role permissions
                Permission.of(ResourceType.ROLE, ActionType.CREATE, "ALL"),
                Permission.of(ResourceType.ROLE, ActionType.READ, "ALL"),
                Permission.of(ResourceType.ROLE, ActionType.UPDATE, "ALL"),
                Permission.of(ResourceType.ROLE, ActionType.DELETE, "ALL"),

                // Permission permissions
                Permission.of(ResourceType.PERMISSION, ActionType.READ, "ALL"),
                Permission.of(ResourceType.PERMISSION, ActionType.ASSIGN, "ALL"),
                Permission.of(ResourceType.PERMISSION, ActionType.CREATE, "ALL"),
                Permission.of(ResourceType.PERMISSION, ActionType.UPDATE, "ALL")
        );

        permissionRepository.saveAll(permissions);
        log.info("Saved {} permissions", permissions.size());
    }

    private void seedRoles() {
        // Cari permissions berdasarkan nama yang PASTI benar
        Map<String, Permission> permissionMap = new HashMap<>();
        permissionRepository.findAll().forEach(p -> permissionMap.put(p.getName(), p));

        // Debug: Tampilkan semua permissions yang tersedia
        log.info("Available permissions:");
        permissionMap.keySet().forEach(name -> log.info("  - {}", name));

        // Buat roles dengan permissions
        Role adminRole = Role.systemRole("ROLE_ADMIN", "ADMIN");
        adminRole.setDescription("System Administrator - Full Access");
        adminRole.setPermissions(new HashSet<>(permissionMap.values()));

        Role employeeRole = Role.businessRole("ROLE_EMPLOYEE", "EMPLOYEE", "Regular Employee");
        employeeRole.setDefaultRole(true);
        employeeRole.setPermissions(new HashSet<>(Arrays.asList(
                permissionMap.get("employee:read:self"),
                permissionMap.get("salary:read:self"),
                permissionMap.get("leave_request:create:all"),
                permissionMap.get("leave_request:read:all")
        )));

        roleRepository.saveAll(Arrays.asList(adminRole, employeeRole));
        log.info("Saved {} roles", roleRepository.count());
    }

    private void seedAdminUser() {
        if (userRepository.findByEmail("admin@hris.com").isEmpty()) {
            Role adminRole = roleRepository.findByCode("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User adminUser = User.builder()
                    .email("admin@hris.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .firstName("Admin")
                    .fullName("System Administrator")
                    .status(UserStatus.ACTIVE)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(Set.of(adminRole))
                    .build();

            userRepository.save(adminUser);
            log.info("Created admin user: admin@hris.com / Admin@123");
        }
    }
}

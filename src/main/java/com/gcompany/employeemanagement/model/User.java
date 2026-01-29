package com.gcompany.employeemanagement.model;

import com.gcompany.employeemanagement.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Core authentication fields
    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    // Basic user information
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "profile_picture", length = 100)
    private String profilePicture;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "departmens_id")
    private Department department;

    // mappedBy menandakan bahwa field 'user' di kelas Profile adalah pemilik foreign key
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    // Status menggunakan enum (lebih clean dan scalable)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    // Simple flags untuk Spring Security
    @Column(name = "is_account_non_expired", nullable = false)
    @Builder.Default
    private boolean accountNonExpired = true;

    @Column(name = "is_account_non_locked", nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(name = "is_credentials_non_expired", nullable = false)
    @Builder.Default
    private boolean credentialsNonExpired = true;

    // Relations - ManyToMany dengan Role
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            foreignKey = @ForeignKey(name = "fk_user_roles_user"),
            inverseForeignKey = @ForeignKey(name = "fk_user_roles_role")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles = new HashSet<>();

    // Metadata
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    // ========== UserDetails Implementation ==========
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add roles as authorities (with ROLE_ prefix for Spring Security)
        roles.forEach(role ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode())));

        // Add permissions as authorities
        roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .forEach(permission ->
                        authorities.add(new SimpleGrantedAuthority(permission.getName())));

        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired && status != UserStatus.INACTIVE;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked && status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    // ========== Helper Methods ==========

    // Check if user has specific role
    public boolean hasRole(String roleCode) {
        return roles.stream()
                .anyMatch(role -> role.getCode().equals(roleCode));
    }

    // Check if user has specific permission
    public boolean hasPermission(String permissionName) {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    // Get all role codes as Set
    public Set<String> getRoleCodes() {
        return roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
    }

    // Get all permission names as Set
    public Set<String> getPermissionNames() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    // Add role to user
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    // Remove role from user
    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }

    // Business methods untuk status management
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void lock() {
        this.status = UserStatus.LOCKED;
        this.accountNonLocked = false;
    }

    public void unlock() {
        if (this.status == UserStatus.LOCKED) {
            this.status = UserStatus.ACTIVE;
            this.accountNonLocked = true;
        }
    }

    public void markAsResigned() {
        this.status = UserStatus.RESIGNED;
        this.accountNonExpired = false;
    }

    // Checker methods untuk status
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isInactive() {
        return status == UserStatus.INACTIVE;
    }

    public boolean isSuspended() {
        return status == UserStatus.SUSPENDED;
    }

    public boolean isLocked() {
        return status == UserStatus.LOCKED;
    }

    public boolean isResigned() {
        return status == UserStatus.RESIGNED;
    }

    // Static factory method untuk create user
    public static User create(String email, String password, String fullName) {
        return User.builder()
                .email(email)
                .password(password)
                .fullName(fullName)
                .status(UserStatus.ACTIVE)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }
}
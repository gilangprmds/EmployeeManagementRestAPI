package com.gcompany.employeemanagement.model;

import com.gcompany.employeemanagement.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_role_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_role_code", columnNames = "code")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;  // Contoh: ROLE_ADMIN, ROLE_HR

    @Column(nullable = false, length = 20)
    private String code;  // Contoh: ADMIN, HR, MANAGER

    @Column(length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RoleType type = RoleType.BUSINESS;

    // Apakah role ini default untuk user baru?
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean defaultRole = false;

    // Apakah role ini sistem? (tidak bisa dihapus/diubah)
    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private boolean system = false;

    // Apakah role ini aktif?
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    // Hierarchical role
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_role_id")
    @ToString.Exclude
    private Role parentRole;

    // Priority untuk sorting
    @Column(name = "priority")
    private Integer priority;

    // Relations
    // ManyToMany dengan Permission
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            foreignKey = @ForeignKey(name = "fk_role_permissions_role"),
            inverseForeignKey = @ForeignKey(name = "fk_role_permissions_permission")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Permission> permissions = new HashSet<>();

    // ManyToMany dengan User (bidirectional)
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> users = new HashSet<>();

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

    // Helper methods
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName) {
        return permissions.stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
        permission.getRoles().add(this);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
        permission.getRoles().remove(this);
    }

    // Static factory methods untuk role umum
    public static Role systemRole(String name, String code) {
        return Role.builder()
                .name(name)
                .code(code)
                .type(RoleType.SYSTEM)
                .system(true)
                .priority(1)
                .build();
    }

    public static Role businessRole(String name, String code, String description) {
        return Role.builder()
                .name(name)
                .code(code)
                .description(description)
                .type(RoleType.BUSINESS)
                .priority(10)
                .build();
    }
}

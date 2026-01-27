package com.gcompany.employeemanagement.model;

import com.gcompany.employeemanagement.enums.ActionType;
import com.gcompany.employeemanagement.enums.ResourceType;
import com.gcompany.employeemanagement.model.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "permissions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_permission_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_resource_action_scope",
                        columnNames = {"resource", "action", "scope"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ResourceType resource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActionType action;

    // Scope permission
    @Column(length = 20)
    @Builder.Default  // Tambahkan default value
    private String scope = "ALL";  // Default scope

    // Apakah permission ini sensitif? (butuh approval khusus)
    @Column(name = "is_sensitive", nullable = false)
    private boolean sensitive = false;

    // Apakah permission ini aktif?
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // Kategori untuk grouping di UI
    @Column(length = 50)
    private String category; // HR, PAYROLL, RECRUITMENT, etc.

    // Relations - ManyToMany dengan Role (bidirectional)
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
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

    // Helper method untuk mendapatkan permission string
    public String getPermissionString() {
        return resource.name() + "_" + action.name();
    }


    // Helper method untuk generate permission name
//    @PrePersist
//    @PreUpdate
//    public void generatePermissionName() {
//        if (this.name == null || this.name.isEmpty()) {
//            this.name = resource.name().toLowerCase() + ":" + action.name().toLowerCase();
//            if (this.scope != null && !this.scope.isEmpty() && !this.scope.equals("ALL")) {
//                this.name += ":" + this.scope.toLowerCase();
//            }
//        }
//    }

    // Static factory method untuk memudahkan pembuatan permission
    public static Permission of(ResourceType resource, ActionType action) {
        return Permission.builder()
                .resource(resource)
                .action(action)
                .scope("ALL")  // Explicitly set scope
                .name(resource.name().toLowerCase() + ":" + action.name().toLowerCase())
                .description(action.name() + " " + resource.name())
                .build();
    }

    public static Permission of(ResourceType resource, ActionType action, String scope) {
        String scopeLower = scope.toLowerCase();
        return Permission.builder()
                .resource(resource)
                .action(action)
                .scope(scope)
                .name(resource.name().toLowerCase() + ":" + action.name().toLowerCase() + ":" + scopeLower)
                .description(action.name() + " " + resource.name() + " (" + scope + ")")
                .build();
    }


}
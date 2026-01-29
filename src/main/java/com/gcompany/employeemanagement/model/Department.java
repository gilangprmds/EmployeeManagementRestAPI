package com.gcompany.employeemanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "departments")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private Set<User> employees = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper method to add employee
    public void addEmployee(User employee) {
        employees.add(employee);
        employee.setDepartment(this);
    }

    // Helper method to remove employee
    public void removeEmployee(User employee) {
        employees.remove(employee);
        employee.setDepartment(null);
    }

    // Pre-update validation
    @PreUpdate
    @PrePersist
    private void validate() {
        if (this.code != null) {
            this.code = this.code.toUpperCase().trim();
        }
        if (this.name != null) {
            this.name = this.name.trim();
        }
    }
}

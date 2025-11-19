package com.gcompany.employeemanagement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "roles")
public class Role {
    @Id @GeneratedValue
    private Long id;
    private String name;

    public Role(String user) {
    }
}


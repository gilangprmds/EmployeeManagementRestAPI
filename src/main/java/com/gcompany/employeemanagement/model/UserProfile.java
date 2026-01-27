package com.gcompany.employeemanagement.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "user_profile")
public class UserProfile {
    @Id
    @GeneratedValue
    private Long id;



    // Address
    private String street;
    private String city;
    private String state;
    private String postalCode;

    // JoinColumn mendefinisikan kolom foreign key di tabel profiles
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;
}

package com.gcompany.employeemanagement.enums;

public enum UserStatus {
    ACTIVE,         // Aktif
    INACTIVE,       // Tidak aktif
    SUSPENDED,      // Ditangguhkan
    LOCKED,         // Terkunci (karena salah password)
    PENDING,        // Menunggu verifikasi
    RESIGNED        // Mengundurkan diri
}

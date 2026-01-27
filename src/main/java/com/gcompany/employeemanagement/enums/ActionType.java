package com.gcompany.employeemanagement.enums;

public enum ActionType {
    CREATE,     // Membuat data baru
    READ,       // Melihat data
    UPDATE,     // Mengubah data
    DELETE,     // Menghapus data
    ACTIVATE,
    DEACTIVATE,
    RESET_PASSWORD,
    PROCESS,

    // HRIS Specific Actions
    APPROVE,    // Menyetujui
    REJECT,     // Menolak
    CANCEL,
    VERIFY,     // Memverifikasi
    EXPORT,     // Mengekspor
    IMPORT,     // Mengimpor
    DOWNLOAD,   // Mengunduh
    UPLOAD,     // Mengunggah
    VIEW,
    SCHEDULE,
    REVIEW,
    CLOCK_IN,
    CLOCK_OUT,
    OVERTIME,
    GENERATE_REPORT,
    PROMOTE,
    TRANSFER,
    TERMINATE,

    // Administrative Actions
    ASSIGN,     // Menugaskan
    DELEGATE,   // Mendelegasikan
    REVOKE,     // Mencabut
    RESET       // Mereset
}
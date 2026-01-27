//package com.gcompany.employeemanagement.config;
//
//import com.gcompany.employeemanagement.enums.Role;
//import com.gcompany.employeemanagement.model.User;
//import com.gcompany.employeemanagement.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class DataInitializer implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder encoder;
//
//    @Override
//    public void run(String... args) {
//        init();
//    }
//
//    private void init() {
//
////        // Buat role jika belum ada
////        Role adminRole = roleRepository.findByName("ADMIN")
////                .orElseGet(() -> roleRepository.save(new Role(null, "ADMIN")));
////
////        Role userRole = roleRepository.findByName("USER")
////                .orElseGet(() -> roleRepository.save(new Role(null, "USER")));
//
//        // Buat user admin default jika belum ada
//        if (userRepository.findByRole(Role.ADMIN).isEmpty()) {
//            User admin = new User();
//            admin.setEmail("admin@email.com");
//            admin.setFullName("Admin");
//            admin.setPassword(encoder.encode("admin123"));
//            admin.setRole(Role.ADMIN);
//            userRepository.save(admin);
//        }
//    }
//}

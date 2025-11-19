package com.gcompany.employeemanagement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/api/me")
    public java.util.Map<String, Object> me(org.springframework.security.core.Authentication auth) {
        if (auth == null) return java.util.Map.of("authenticated", false);
        return java.util.Map.of("authenticated", true, "principal", auth.getName(), "authorities", auth.getAuthorities());
    }

    @GetMapping("/admin/secret")
    public String adminOnly() {
        return "Admin secret data";
    }
}

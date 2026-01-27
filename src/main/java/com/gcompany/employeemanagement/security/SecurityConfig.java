package com.gcompany.employeemanagement.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF karena kita menggunakan JWT (stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS Configuration
                .cors(Customizer.withDefaults())

                // Session Management (stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Exception Handling
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                                .accessDeniedHandler(new JwtAccessDeniedHandler()))

                // Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (tidak perlu authentication)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Health check & monitoring
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // ========== HRIS SPECIFIC RULES ==========

                        // User Management
                        .requestMatchers(HttpMethod.GET, "/api/users/profile/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/profile/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")


                        // ========== ROLE & PERMISSION MANAGEMENT ==========
                        .requestMatchers(HttpMethod.GET, "/api/roles/active").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.GET, "/api/roles/default").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.GET, "/api/roles/code/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.GET, "/api/roles/search").hasAnyRole("ADMIN", "HR")
                        .requestMatchers("/api/roles/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/permissions/active").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.GET, "/api/permissions/resource/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.GET, "/api/permissions/action/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.GET, "/api/permissions/category/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.GET, "/api/permissions/role/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.GET, "/api/permissions/search").hasAnyRole("ADMIN", "HR")
                        .requestMatchers("/api/permissions/**").hasRole("ADMIN")

                        // Employee Data (berdasarkan role)
                        .requestMatchers(HttpMethod.GET, "/api/employees/me").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/employees/**").hasAnyRole("HR", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/employees").hasAnyRole("HR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasAnyRole("HR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("ADMIN")

                        // Salary (sensitive - strict access)
                        .requestMatchers(HttpMethod.GET, "/api/salaries/me").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/salaries/**").hasAnyRole("HR", "PAYROLL", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/salaries").hasAnyRole("HR", "PAYROLL", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/salaries/**").hasAnyRole("HR", "PAYROLL", "ADMIN")

                        // Leave Requests
                        .requestMatchers(HttpMethod.GET, "/api/leaves/me").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/leaves").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/leaves/**/approve").hasAnyRole("MANAGER", "HR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/leaves/**").hasAnyRole("HR", "MANAGER", "ADMIN")

                        // Attendance
                        .requestMatchers("/api/attendance/me/**").hasRole("EMPLOYEE")
                        .requestMatchers("/api/attendance/checkin").hasRole("EMPLOYEE")
                        .requestMatchers("/api/attendance/checkout").hasRole("EMPLOYEE")
                        .requestMatchers("/api/attendance/history").hasRole("EMPLOYEE")
                        .requestMatchers("/api/attendance/today").hasRole("EMPLOYEE")
                        .requestMatchers("/api/attendance/**").hasAnyRole("HR", "MANAGER", "ADMIN")


                        // Default: Semua request lainnya membutuhkan authentication
                        .anyRequest().authenticated()
                )

                // Authentication Provider
                .authenticationProvider(authenticationProvider())

                // JWT Filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength 12 (secure)
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of(
//                "http://localhost:5173",  // React dev
//                "https://employee.gcompany.com" // Production
//        ));
//        configuration.setAllowedMethods(Arrays.asList(
//                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
//        ));
//        configuration.setAllowedHeaders(Arrays.asList(
//                "Authorization", "Content-Type", "X-Requested-With",
//                "Accept", "Origin", "Access-Control-Request-Method",
//                "Access-Control-Request-Headers"
//        ));
//        configuration.setExposedHeaders(Arrays.asList(
//                "Authorization", "Content-Disposition"
//        ));
//        configuration.setAllowCredentials(true);
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
}
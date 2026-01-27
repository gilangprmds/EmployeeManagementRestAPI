package com.gcompany.employeemanagement.security;

import com.gcompany.employeemanagement.model.User;
import com.gcompany.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User with username '%s' not found", username)));

        log.debug("Loaded user: {} with roles: {}",
                username, user.getRoleCodes());

        return user;
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User with ID '%s' not found", userId)));

        log.debug("Loaded user by ID: {} with roles: {}",
                userId, user.getRoleCodes());

        return user;
    }
}
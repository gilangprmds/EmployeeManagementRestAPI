package com.gcompany.employeemanagement.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

public class SecurityExceptions {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class JwtAuthenticationException extends AuthenticationException {
        public JwtAuthenticationException(String msg) {
            super(msg);
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class InvalidJwtTokenException extends JwtAuthenticationException {
        public InvalidJwtTokenException(String msg) {
            super(msg);
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class ExpiredJwtTokenException extends JwtAuthenticationException {
        public ExpiredJwtTokenException(String msg) {
            super(msg);
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class AccessDeniedException extends AuthenticationException {
        public AccessDeniedException(String msg) {
            super(msg);
        }
    }
}
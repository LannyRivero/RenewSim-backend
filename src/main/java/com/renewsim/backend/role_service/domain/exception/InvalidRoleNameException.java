package com.renewsim.backend.role_service.domain.exception;

public class InvalidRoleNameException extends RuntimeException {
    public InvalidRoleNameException(String message) {
        super(message);
    }
}


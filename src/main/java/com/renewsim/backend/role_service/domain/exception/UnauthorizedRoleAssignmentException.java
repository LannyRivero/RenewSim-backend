package com.renewsim.backend.role_service.domain.exception;

public class UnauthorizedRoleAssignmentException extends RuntimeException {
    public UnauthorizedRoleAssignmentException(String message) {
        super(message);
    }
}


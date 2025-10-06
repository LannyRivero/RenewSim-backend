package com.renewsim.backend.role_service.domain.exception;

public class DuplicateRoleException extends RuntimeException {
    public DuplicateRoleException(String message) {
        super(message);
    }
}


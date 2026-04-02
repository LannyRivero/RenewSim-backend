package com.renewsim.backend.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Generic exception for resource conflict scenarios (duplicate entities, constraint violations).
 * Maps to HTTP 409 Conflict.
 * 
 * Use for scenarios like duplicate usernames, role names, or business rule violations
 * that prevent resource creation/modification.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
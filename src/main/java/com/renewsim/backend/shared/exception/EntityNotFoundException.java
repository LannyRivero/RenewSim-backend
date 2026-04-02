package com.renewsim.backend.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Generic exception for entity not found scenarios across all bounded contexts.
 * Maps to HTTP 404 Not Found.
 * 
 * Replaces domain-specific exceptions like UserNotFoundException, RoleNotFoundException, etc.
 * for consistent API responses.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {
    
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.renewsim.backend.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when the AI service is temporarily unavailable or unresponsive.
 * Maps to HTTP 503 Service Unavailable.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class AIServiceUnavailableException extends RuntimeException {
    
    public AIServiceUnavailableException(String message) {
        super(message);
    }
    
    public AIServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.renewsim.backend.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a client exceeds the allowed rate limit for API
 * requests.
 * Maps to HTTP 429 Too Many Requests.
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException {

    private final Integer retryAfterSeconds;

    public RateLimitExceededException(String message) {
        super(message);
        this.retryAfterSeconds = null;
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
        this.retryAfterSeconds = null;
    }

    public RateLimitExceededException(String message, int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

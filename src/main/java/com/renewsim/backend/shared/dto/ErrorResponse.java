package com.renewsim.backend.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a standardized error response structure used across all RenewSim
 * services.
 * Includes observability (correlationId), timestamp, actor (current user), and
 * validation details.
 * 
 * Envelope structure:
 * {
 * "timestamp": "2026-04-02T14:30:00Z",
 * "status": 404,
 * "errorCode": "ENTITY_NOT_FOUND",
 * "error": "Not Found",
 * "message": "User with id 123 not found",
 * "path": "/api/v1/users/123",
 * "correlationId": "abc-123-def",
 * "actor": "admin@renewsim.com",
 * "fieldErrors": { "email": "must be valid" }
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int status;
    private final String errorCode;
    private final String error;
    private final String message;
    private final String path;
    private final Instant timestamp;
    private final String correlationId;
    private final String actor;
    private final Map<String, String> fieldErrors;

    private ErrorResponse(Builder b) {
        this.status = b.status;
        this.errorCode = b.errorCode;
        this.error = b.error;
        this.message = b.message;
        this.path = b.path;
        this.timestamp = b.timestamp != null ? b.timestamp : Instant.now();
        this.correlationId = b.correlationId;
        this.actor = b.actor;
        this.fieldErrors = b.fieldErrors;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ---------------------------------------
    // Getters
    // ---------------------------------------
    public int getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getActor() {
        return actor;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    // ---------------------------------------
    // Builder pattern
    // ---------------------------------------
    public static final class Builder {
        private int status;
        private String errorCode;
        private String error;
        private String message;
        private String path;
        private Instant timestamp;
        private String correlationId;
        private String actor;
        private Map<String, String> fieldErrors;

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder actor(String actor) {
            this.actor = actor;
            return this;
        }

        public Builder fieldErrors(Map<String, String> fieldErrors) {
            this.fieldErrors = fieldErrors;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}

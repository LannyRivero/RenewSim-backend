package com.renewsim.backend.shared.exception.handler;

import com.renewsim.backend.shared.dto.ErrorResponse;
import com.renewsim.backend.shared.observability.TraceUtils;
import com.renewsim.backend.shared.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Map;

/**
 * Base class for all exception handlers in RenewSim.
 * Provides shared utility method for building consistent ErrorResponse
 * envelopes.
 */
public abstract class BaseExceptionHandler {

    protected static final String ANONYMOUS_ACTOR = "anonymous";

    /**
     * Builds a standardized ErrorResponse with full traceability.
     *
     * @param status    HTTP status code
     * @param errorCode Machine-readable error code (e.g., VALIDATION_ERROR,
     *                  ENTITY_NOT_FOUND)
     * @param error     Human-readable error category
     * @param message   Detailed error message
     * @param req       HTTP request for extracting path
     * @param fields    Optional field-level validation errors
     * @return ResponseEntity with ErrorResponse body
     */
    protected ResponseEntity<ErrorResponse> buildError(
            HttpStatus status,
            String errorCode,
            String error,
            String message,
            HttpServletRequest req,
            Map<String, String> fields) {
        String currentActor = SecurityUtils.currentUsername();

        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .errorCode(errorCode)
                .error(error)
                .message(message)
                .path(req.getRequestURI())
                .timestamp(Instant.now())
                .correlationId(TraceUtils.currentCorrelationId())
                .actor(currentActor != null ? currentActor : ANONYMOUS_ACTOR)
                .fieldErrors(fields)
                .build();

        return new ResponseEntity<>(response, status);
    }
}

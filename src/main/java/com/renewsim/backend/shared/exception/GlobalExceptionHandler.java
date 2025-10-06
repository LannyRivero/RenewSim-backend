package com.renewsim.backend.shared.exception;

import com.renewsim.backend.role_service.domain.exception.LastAdminRemovalException;
import com.renewsim.backend.shared.dto.ErrorResponse;
import com.renewsim.backend.shared.observability.TraceUtils;
import com.renewsim.backend.shared.security.SecurityUtils;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.TypeMismatchException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized error handler for all controllers across RenewSim microservices.
 * Ensures consistent ErrorResponse structure, with traceability and actor context.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // ---------------------------------
    // 🔹 USER exceptions
    // ---------------------------------
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, "User not found", ex.getMessage(), null);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return buildError(HttpStatus.CONFLICT, "User already exists", ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserData(InvalidUserDataException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Invalid user data", ex.getMessage(), null);
    }

    // ---------------------------------
    // 🔹 ROLE exceptions
    // ---------------------------------
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFound(RoleNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, "Role not found", ex.getMessage(), null);
    }

    @ExceptionHandler(RoleAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleRoleAlreadyExists(RoleAlreadyExistsException ex) {
        return buildError(HttpStatus.CONFLICT, "Role already exists", ex.getMessage(), null);
    }

    @ExceptionHandler(LastAdminRemovalException.class)
    public ResponseEntity<ErrorResponse> handleLastAdminRemoval(LastAdminRemovalException ex) {
        return buildError(HttpStatus.CONFLICT, "Last admin removal not allowed", ex.getMessage(), null);
    }

    // ---------------------------------
    // 🔹 VALIDATION errors
    // ---------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage())
        );
        return buildError(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid request body", fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv ->
                fieldErrors.put(cv.getPropertyPath().toString(), cv.getMessage())
        );
        return buildError(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid request parameter", fieldErrors);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getFieldErrors().forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));
        return buildError(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid request parameter", fieldErrors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerValidation(HandlerMethodValidationException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getAllValidationResults().forEach(result ->
                result.getResolvableErrors().forEach(err ->
                        fieldErrors.put(result.getMethodParameter().getParameterName(), err.getDefaultMessage()))
        );
        return buildError(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid method argument", fieldErrors);
    }

    // ---------------------------------
    // 🔹 PATH / TYPE / CONVERSION
    // ---------------------------------
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            ConversionFailedException.class,
            TypeMismatchException.class,
            MissingPathVariableException.class,
            NumberFormatException.class
    })
    public ResponseEntity<ErrorResponse> handleBadParams(Exception ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Validation Error", ex.getMessage(), null);
    }

    // ---------------------------------
    // 🔹 HTTP / Servlet / General
    // ---------------------------------
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            HttpRequestMethodNotSupportedException.class,
            NoHandlerFoundException.class,
            ResponseStatusException.class,
            ServletException.class
    })
    public ResponseEntity<ErrorResponse> handleHttpExceptions(Exception ex) {
        HttpStatus status = resolveStatus(ex);
        return buildError(status, status.getReasonPhrase(), ex.getMessage(), null);
    }

    // ---------------------------------
    // 🔹 FALLBACK
    // ---------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), null);
    }

    // ---------------------------------
    // 🔧 Utility builder
    // ---------------------------------
    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String error, String message, Map<String, String> fields) {
        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .timestamp(Instant.now())
                .correlationId(TraceUtils.currentTraceId())
                .actor(SecurityUtils.currentUsername())
                .fieldErrors(fields)
                .build();

        return new ResponseEntity<>(response, status);
    }

    private HttpStatus resolveStatus(Exception ex) {
        if (ex instanceof HttpRequestMethodNotSupportedException) return HttpStatus.METHOD_NOT_ALLOWED;
        if (ex instanceof NoHandlerFoundException) return HttpStatus.NOT_FOUND;
        if (ex instanceof HttpMessageNotReadableException) return HttpStatus.BAD_REQUEST;
        if (ex instanceof ResponseStatusException rse) return HttpStatus.valueOf(rse.getStatusCode().value());
        if (ex instanceof ServletException) return HttpStatus.INTERNAL_SERVER_ERROR;
        return HttpStatus.BAD_REQUEST;
    }
}

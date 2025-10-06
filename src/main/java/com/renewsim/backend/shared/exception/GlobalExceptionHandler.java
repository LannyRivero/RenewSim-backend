package com.renewsim.backend.shared.exception;

import com.renewsim.backend.role_service.domain.exception.LastAdminRemovalException;
import com.renewsim.backend.shared.dto.ErrorResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
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

@ControllerAdvice
public class GlobalExceptionHandler {

        // --------------------------
        // User exceptions
        // --------------------------
        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
                return buildResponse(HttpStatus.NOT_FOUND, "User not found", ex.getMessage(), request.getRequestURI(),
                                null);
        }

        @ExceptionHandler(UserAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.CONFLICT, "User already exists", ex.getMessage(),
                                request.getRequestURI(),
                                null);
        }

        @ExceptionHandler(InvalidUserDataException.class)
        public ResponseEntity<ErrorResponse> handleInvalidUserData(InvalidUserDataException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Invalid user data", ex.getMessage(),
                                request.getRequestURI(),
                                null);
        }

        // --------------------------
        // Role exceptions
        // --------------------------
        @ExceptionHandler(RoleNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleRoleNotFound(RoleNotFoundException ex, HttpServletRequest request) {
                return buildResponse(HttpStatus.NOT_FOUND, "Role not found", ex.getMessage(), request.getRequestURI(),
                                null);
        }

        @ExceptionHandler(RoleAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleRoleAlreadyExists(RoleAlreadyExistsException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.CONFLICT, "Role already exists", ex.getMessage(),
                                request.getRequestURI(),
                                null);
        }

        @ExceptionHandler(LastAdminRemovalException.class)
        public ResponseEntity<ErrorResponse> handleLastAdminRemoval(LastAdminRemovalException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.CONFLICT, "Last admin removal not allowed",
                                ex.getMessage(), request.getRequestURI(), null);
        }

        // --------------------------
        // Validation errors
        // --------------------------
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                Map<String, String> fieldErrors = new HashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

                return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid request body",
                                request.getRequestURI(), fieldErrors);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolations(ConstraintViolationException ex,
                        HttpServletRequest request) {
                Map<String, String> fieldErrors = new HashMap<>();
                ex.getConstraintViolations()
                                .forEach(cv -> fieldErrors.put(cv.getPropertyPath().toString(), cv.getMessage()));

                return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid request parameter",
                                request.getRequestURI(), fieldErrors);
        }

        // --------------------------
        // Path variable / type mismatch
        // --------------------------
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error",
                                "Invalid format for parameter '" + ex.getName() + "': " + ex.getValue(),
                                request.getRequestURI(), null);
        }

        @ExceptionHandler(MissingPathVariableException.class)
        public ResponseEntity<ErrorResponse> handleMissingPathVariable(MissingPathVariableException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error",
                                "Missing path variable: " + ex.getVariableName(),
                                request.getRequestURI(), null);
        }

        // --------------------------
        // ResponseStatusException (genérica)
        // --------------------------
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.valueOf(ex.getStatusCode().value()), "Response status exception",
                                ex.getReason() != null ? ex.getReason() : "Unexpected error",
                                request.getRequestURI(), null);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed",
                                ex.getMessage(), request.getRequestURI(), null);
        }

        @ExceptionHandler(NoHandlerFoundException.class)
        public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.NOT_FOUND, "Endpoint not found",
                                ex.getMessage(), request.getRequestURI(), null);
        }

        @ExceptionHandler(ConversionFailedException.class)
        public ResponseEntity<ErrorResponse> handleConversionFailed(ConversionFailedException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error",
                                "Failed to convert parameter: " + ex.getValue(),
                                request.getRequestURI(), null);
        }

        @ExceptionHandler(TypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatchGeneric(TypeMismatchException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error",
                                "Type mismatch error: " + ex.getMessage(),
                                request.getRequestURI(), null);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error",
                                "Malformed request body",
                                request.getRequestURI(), null);
        }

        @ExceptionHandler(ServletException.class)
        public ResponseEntity<ErrorResponse> handleServletException(ServletException ex, HttpServletRequest request) {
                // Heurística: si contiene "Failed to convert" lo tratamos como 400
                if (ex.getMessage() != null && ex.getMessage().contains("Failed to convert")) {
                        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error",
                                        ex.getMessage(), request.getRequestURI(), null);
                }
                return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                                ex.getMessage(), request.getRequestURI(), null);
        }

        @ExceptionHandler(BindException.class)
        public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
                Map<String, String> fieldErrors = new HashMap<>();
                ex.getFieldErrors().forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));
                return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error",
                                "Invalid request parameter", request.getRequestURI(), fieldErrors);
        }

        @ExceptionHandler(HandlerMethodValidationException.class)
        public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex,
                        HttpServletRequest request) {
                Map<String, String> fieldErrors = new HashMap<>();
                ex.getAllValidationResults().forEach(result -> result.getResolvableErrors().forEach(err -> fieldErrors
                                .put(result.getMethodParameter().getParameterName(), err.getDefaultMessage())));
                return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error",
                                "Invalid method argument", request.getRequestURI(), fieldErrors);
        }

        @ExceptionHandler(NumberFormatException.class)
        public ResponseEntity<ErrorResponse> handleNumberFormat(NumberFormatException ex, HttpServletRequest request) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error",
                                "Invalid numeric value: " + ex.getMessage(),
                                request.getRequestURI(), null);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
                return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                                ex.getMessage(),
                                request.getRequestURI(), null);
        }

        // --------------------------
        // Builder util
        // --------------------------
        private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status,
                        String error,
                        String message,
                        String path,
                        Map<String, String> fieldErrors) {
                ErrorResponse response = ErrorResponse.builder()
                                .status(status.value())
                                .error(error)
                                .message(message)
                                .path(path)
                                .timestamp(Instant.now())
                                .correlationId(null) // si usas MDC puedes pasar traceId aquí
                                .fieldErrors(fieldErrors)
                                .build();

                return new ResponseEntity<>(response, status);
        }
}

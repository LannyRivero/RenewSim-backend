package com.renewsim.backend.auth_service.web.controller;

import com.renewsim.backend.shared.dto.ErrorResponse;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.ResourceConflictException;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

        private static final String CORRELATION_KEY = "correlationId";
        private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

        @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
        public ResponseEntity<ErrorResponse> handleAuth401(Exception ex, HttpServletRequest req) {
                return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req, null);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handle403(AccessDeniedException ex, HttpServletRequest req) {
                return build(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), req, null);
        }

        @ExceptionHandler(ResourceConflictException.class)
        public ResponseEntity<ErrorResponse> handle409(ResourceConflictException ex, HttpServletRequest req) {
                return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req, null);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handle400Validation(MethodArgumentNotValidException ex,
                        HttpServletRequest req) {
                Map<String, String> fields = new LinkedHashMap<>();
                for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
                        fields.put(fe.getField(), fe.getDefaultMessage());
                }
                return build(HttpStatus.BAD_REQUEST, "Bad Request", "Validation failed", req, fields);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handle400Readable(HttpMessageNotReadableException ex,
                        HttpServletRequest req) {
                return build(HttpStatus.BAD_REQUEST, "Bad Request", "Malformed request body", req, null);
        }

        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handle415(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
                return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", ex.getMessage(), req, null);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handle405(HttpRequestMethodNotSupportedException ex,
                        HttpServletRequest req) {
                return build(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", ex.getMessage(), req, null);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handle500(Exception ex, HttpServletRequest req) {
                log.error("❌ Unexpected error at {} {} [correlationId={}]",
                                req.getMethod(), req.getRequestURI(), MDC.get(CORRELATION_KEY), ex);

                return build(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Internal Server Error",
                                "Unexpected error",
                                req,
                                null);
        }

        private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message,
                        HttpServletRequest req, Map<String, String> fieldErrors) {
                ErrorResponse body = ErrorResponse.builder()
                                .status(status.value())
                                .error(error)
                                .message(message)
                                .path(req.getRequestURI())
                                .correlationId(MDC.get(CORRELATION_KEY))
                                .fieldErrors(fieldErrors)
                                .build();

                return ResponseEntity.status(status)
                                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                .body(body);
        }
}

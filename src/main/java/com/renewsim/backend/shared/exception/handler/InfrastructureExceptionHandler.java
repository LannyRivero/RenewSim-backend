package com.renewsim.backend.shared.exception.handler;

import com.renewsim.backend.shared.dto.ErrorResponse;
import com.renewsim.backend.shared.exception.AIServiceUnavailableException;
import com.renewsim.backend.shared.exception.RateLimitExceededException;
import com.renewsim.backend.shared.observability.TraceUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Handles infrastructure and system-level exceptions (HTTP 405, 429, 500, 503).
 * Includes rate limiting, service unavailability, and generic fallback errors.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class InfrastructureExceptionHandler extends BaseExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(InfrastructureExceptionHandler.class);

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(RateLimitExceededException ex, HttpServletRequest req) {
        return buildError(
                HttpStatus.TOO_MANY_REQUESTS,
                "RATE_LIMIT_EXCEEDED",
                "Too many requests",
                ex.getMessage(),
                req,
                null);
    }

    @ExceptionHandler(AIServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleAIServiceUnavailable(AIServiceUnavailableException ex,
            HttpServletRequest req) {
        return buildError(
                HttpStatus.SERVICE_UNAVAILABLE,
                "AI_SERVICE_UNAVAILABLE",
                "AI service unavailable",
                ex.getMessage(),
                req,
                null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest req) {
        return buildError(
                HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                "Method not allowed",
                ex.getMessage(),
                req,
                null);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest req) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "ENDPOINT_NOT_FOUND",
                "Endpoint not found",
                ex.getMessage(),
                req,
                null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return buildError(
                status,
                status.name().replace(" ", "_").toUpperCase(),
                status.getReasonPhrase(),
                ex.getReason() != null ? ex.getReason() : ex.getMessage(),
                req,
                null);
    }

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ErrorResponse> handleServlet(ServletException ex, HttpServletRequest req) {
        log.error("❌ Servlet error at {} {} [correlationId={}]",
                req.getMethod(), req.getRequestURI(), TraceUtils.currentTraceId(), ex);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "SERVLET_ERROR",
                "Internal server error",
                "A servlet processing error occurred",
                req,
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("❌ Unexpected error at {} {} [correlationId={}]",
                req.getMethod(), req.getRequestURI(), TraceUtils.currentTraceId(), ex);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Internal server error",
                "An unexpected error occurred",
                req,
                null);
    }
}

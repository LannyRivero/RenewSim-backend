package com.renewsim.backend.shared.exception.handler;

import com.renewsim.backend.shared.dto.ErrorResponse;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.ForbiddenException;
import com.renewsim.backend.shared.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Handles security and authentication exceptions (HTTP 401, 403).
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class SecurityExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler({
            AuthenticationException.class,
            UnauthorizedException.class
    })
    public ResponseEntity<ErrorResponse> handleUnauthorized(RuntimeException ex, HttpServletRequest req) {
        return buildError(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Unauthorized",
                ex.getMessage(),
                req,
                null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return buildError(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Access denied",
                ex.getMessage(),
                req,
                null);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        return buildError(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Access denied",
                ex.getMessage(),
                req,
                null);
    }
}

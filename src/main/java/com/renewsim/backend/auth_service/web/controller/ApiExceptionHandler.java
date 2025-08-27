package com.renewsim.backend.auth_service.web.controller;

import com.renewsim.backend.exception.UnauthorizedException;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.ResourceConflictException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<Map<String, Object>> handleAuth(AuthenticationException ex, HttpServletRequest req) {
                audit("AUTH_FAILURE", ex.getMessage(), req);
                return json(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req, null);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex,
                        HttpServletRequest req) {
                audit("BAD_CREDENTIALS", ex.getMessage(), req);
                return json(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid username or password", req, null);
        }

        @ExceptionHandler(ResourceConflictException.class)
        public ResponseEntity<Map<String, Object>> handleConflict(ResourceConflictException ex,
                        HttpServletRequest req) {
                audit("CONFLICT", ex.getMessage(), req);
                return json(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req, null);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<Map<String, Object>> handleForbidden(AccessDeniedException ex, HttpServletRequest req) {
                audit("FORBIDDEN", ex.getMessage(), req);
                return json(HttpStatus.FORBIDDEN, "Forbidden", "Access is denied", req, null);
        }

        @ExceptionHandler({ MethodArgumentNotValidException.class, HttpMessageNotReadableException.class })
        public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex, HttpServletRequest req) {
                audit("BAD_REQUEST", ex.getMessage(), req);
                return json(HttpStatus.BAD_REQUEST, "Bad Request", "Invalid request payload", req, null);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest req) {
                audit("UNHANDLED", ex.getMessage(), req);
                return json(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error", req, null);
        }

        @ExceptionHandler(com.renewsim.backend.exception.UnauthorizedException.class)
        public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex,
                        HttpServletRequest req) {
                // audit("UNAUTHORIZED", ex.getMessage(), req); // si quieres log
                return json(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req, null);
        }

        private ResponseEntity<Map<String, Object>> json(HttpStatus status,
                        String error,
                        String message,
                        HttpServletRequest req,
                        Integer retryAfterSeconds) {
                HttpHeaders headers = noStoreHeaders();
                if (retryAfterSeconds != null && retryAfterSeconds >= 0) {
                        headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
                }
                Map<String, Object> body = payload(status.value(), error, message, req.getRequestURI());
                return ResponseEntity.status(status)
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(headers)
                                .body(body);
        }

        private Map<String, Object> payload(int status, String error, String message, String path) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("timestamp", Instant.now().toString());
                m.put("status", status);
                m.put("error", error);
                m.put("message", message);
                m.put("path", path);
                return m;
        }

        private HttpHeaders noStoreHeaders() {
                HttpHeaders h = new HttpHeaders();
                h.set(HttpHeaders.CACHE_CONTROL, "no-store, max-age=0");
                h.set(HttpHeaders.PRAGMA, "no-cache");
                h.set(HttpHeaders.EXPIRES, "0");
                return h;
        }

        private void audit(String event, String reason, HttpServletRequest req) {
                String correlationId = Optional.ofNullable(MDC.get("correlationId"))
                                .orElse(req.getHeader("X-Correlation-Id"));
                String ip = clientIp(req);
                String path = req.getRequestURI();
                String method = req.getMethod();

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String user = (auth != null && auth.isAuthenticated()) ? safePrincipal(auth.getPrincipal())
                                : "anonymous";

                log.warn("event={}, user={}, ip={}, method={}, path={}, correlationId={}, reason={}",
                                event, user, ip, method, path, correlationId, reason);
        }

        private String clientIp(HttpServletRequest request) {
                String xff = request.getHeader("X-Forwarded-For");
                if (xff != null && !xff.isBlank()) {
                        int idx = xff.indexOf(',');
                        return (idx > 0 ? xff.substring(0, idx) : xff).trim();
                }
                return request.getRemoteAddr();
        }

        private String safePrincipal(Object principal) {
                try {
                        return String.valueOf(principal);
                } catch (Exception ignored) {
                        return "unknown";
                }
        }
}

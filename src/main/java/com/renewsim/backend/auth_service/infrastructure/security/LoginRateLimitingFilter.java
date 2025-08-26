package com.renewsim.backend.auth_service.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.config.SecurityRateLimitProperties;
import com.renewsim.backend.auth_service.web.dto.LoginUsernameProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Order(5)
public class LoginRateLimitingFilter extends OncePerRequestFilter {

    private final SecurityRateLimitProperties props;
    private final LoginRateLimiter limiter;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public LoginRateLimitingFilter(SecurityRateLimitProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        // Adapt: Duration -> seconds
        int windowSeconds = Math.toIntExact(props.getWindow().toSeconds());
        int maxAttempts = props.getMaxAttempts();
        this.limiter = new LoginRateLimiter(windowSeconds, maxAttempts);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!props.isEnabled()) return true;
        String path = request.getRequestURI();
        return !pathMatcher.match(props.getLoginPath(), path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrapped = (req instanceof ContentCachingRequestWrapper)
                ? (ContentCachingRequestWrapper) req
                : new ContentCachingRequestWrapper(req);

        String key = buildKey(wrapped);

        if (!limiter.allow(key)) {
            int retry = Math.max(
                Math.toIntExact(props.getRetryAfter().toSeconds()),
                limiter.secondsUntilWindowReset()
            );
            res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
            res.setHeader("Retry-After", String.valueOf(retry));
            // Defensa en profundidad para respuestas de auth:
            res.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Expires", "0");
            return;
        }

        chain.doFilter(wrapped, res);
    }

    private String buildKey(ContentCachingRequestWrapper req) {
        String ip = clientIp(req);

        if (props.getStrategy() == SecurityRateLimitProperties.Strategy.IP_USER) {
            try {
                byte[] buf = req.getContentAsByteArray();
                if (buf != null && buf.length > 0) {
                    String body = new String(buf, StandardCharsets.UTF_8);
                    LoginUsernameProbe probe = objectMapper.readValue(body, LoginUsernameProbe.class);
                    String user = probe.getUsername();
                    if (user != null && !user.isBlank()) {
                        return ip + "|" + user.toLowerCase();
                    }
                }
            } catch (Exception ignored) {
                // deliberately ignore parsing errors to avoid leaking info/timing
            }
        }
        return ip;
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int idx = xff.indexOf(',');
            return (idx > 0 ? xff.substring(0, idx) : xff).trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "0.0.0.0";
    }
}

package com.renewsim.backend.auth_service.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.config.SecurityRateLimitProperties;
import com.renewsim.backend.auth_service.web.dto.LoginUsernameProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class LoginRateLimitingFilter extends OncePerRequestFilter {

    private final SecurityRateLimitProperties props;
    private final LoginRateLimiter limiter;
    private final ObjectMapper objectMapper;

    private AntPathRequestMatcher loginMatcher; 

    public LoginRateLimitingFilter(SecurityRateLimitProperties props,
                                   ObjectMapper objectMapper,
                                   LoginRateLimiter limiter) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.limiter = limiter;
    }

    @Override
    protected void initFilterBean() {
        this.loginMatcher = new AntPathRequestMatcher(props.getLoginPath(), "POST");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!props.isEnabled()) return true;
        return !loginMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        String key;
        if (props.getStrategy() == SecurityRateLimitProperties.Strategy.IP_USER
                && isJson(req.getContentType())) {
            // Solo envuelve si vas a leer body
            ContentCachingRequestWrapper wrapped = (req instanceof ContentCachingRequestWrapper)
                    ? (ContentCachingRequestWrapper) req
                    : new ContentCachingRequestWrapper(req);
            key = buildKeyIpUser(wrapped);
            chainOrLimit(wrapped, res, chain, key);
        } else {
            key = clientIp(req);
            chainOrLimit(req, res, chain, key);
        }
    }

    private void chainOrLimit(HttpServletRequest request,
                              HttpServletResponse response,
                              FilterChain chain,
                              String key) throws IOException, ServletException {
        if (!limiter.allow(key)) {
            int retryAfter = Math.max(
                    Math.toIntExact(props.getRetryAfter().toSeconds()),
                    Math.max(0, limiter.secondsUntilWindowReset())
            );
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isJson(String contentType) {
        return contentType != null
                && contentType.toLowerCase().startsWith(MediaType.APPLICATION_JSON_VALUE);
    }

    private String buildKeyIpUser(ContentCachingRequestWrapper req) {
        String ip = clientIp(req);
        try {
            byte[] buf = req.getContentAsByteArray();
            if (buf != null && buf.length > 0) {
                String body = new String(buf, 0, Math.min(buf.length, 16 * 1024), StandardCharsets.UTF_8);
                LoginUsernameProbe probe = objectMapper.readValue(body, LoginUsernameProbe.class);
                String user = Optional.ofNullable(probe.getUsername())
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .orElse(null);
                if (user != null && !user.isEmpty()) {
                    return ip + "|" + user;
                }
            }
        } catch (Exception ignored) {
        }
        return ip;
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int idx = xff.indexOf(',');
            return (idx > 0 ? xff.substring(0, idx) : xff).trim();
        }
        String remote = request.getRemoteAddr();
        return (remote != null && !remote.isBlank()) ? remote : "0.0.0.0";
    }
}



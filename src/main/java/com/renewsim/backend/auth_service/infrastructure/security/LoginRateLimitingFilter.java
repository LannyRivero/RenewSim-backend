package com.renewsim.backend.auth_service.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.NonNull;
import com.renewsim.backend.auth_service.infrastructure.config.SecurityRateLimitProperties;
import com.renewsim.backend.shared.web.ClientIpExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class LoginRateLimitingFilter extends OncePerRequestFilter {

    private final LoginRateLimiter rateLimiter;             
    private final ObjectMapper objectMapper;                 
    private final SecurityRateLimitProperties props;         
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith(props.getLoginPath())) return true;

        String ct = request.getContentType();
        if (ct == null) return true;
        return !MediaType.parseMediaType(ct).isCompatibleWith(MediaType.APPLICATION_JSON);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request);

        try {
            final String ip = ClientIpExtractor.clientIp(wrapped);
            final String username = extractUsernameSafe(wrapped);

            final String key = rateLimiter.buildKey(ip, username);
            final boolean allowed = rateLimiter.tryAcquire(key);

            if (!allowed) {
                int retryAfter = Math.max(1, rateLimiter.secondsUntilReset(key));

                var body = com.renewsim.backend.shared.dto.ErrorResponse.builder()
                        .status(429)
                        .error("Too Many Requests")
                        .message("Login rate limit exceeded. Please try again later.")
                        .path(request.getRequestURI())
                        .build();

                response.setStatus(429);
                response.setHeader("Retry-After", String.valueOf(retryAfter));
                response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                response.getOutputStream().write(objectMapper.writeValueAsBytes(body));

                log.warn("Rate limit exceeded key={}, ip={}, user={}, retryAfter={}s, window={}s, threshold={}",
                        key, ip, username, retryAfter, props.getWindowSeconds(), props.getThreshold());
                return;
            }

            chain.doFilter(wrapped, response);

        } catch (Exception e) {
            log.warn("LoginRateLimitingFilter error: {}", e.getMessage());
            chain.doFilter(wrapped, response);
        }
    }

    private String extractUsernameSafe(ContentCachingRequestWrapper req) {
        try {
            byte[] buf = req.getContentAsByteArray();
            if (buf == null || buf.length == 0) return null;
            String json = new String(buf, StandardCharsets.UTF_8);
            if (json.isBlank()) return null;

            com.renewsim.backend.auth_service.infrastructure.security.LoginUsernameProbe probe =
                    objectMapper.readValue(json, com.renewsim.backend.auth_service.infrastructure.security.LoginUsernameProbe.class);
            String u = probe.getUsername();
            return (u == null || u.isBlank()) ? null : u.trim();
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("Could not parse username from request body: {}", ex.getMessage());
            }
            return null;
        }
    }
}

package com.renewsim.backend.auth_service.infrastructure.security;

import com.renewsim.backend.auth_service.application.port.out.TokenBlacklistPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Pattern BEARER_PATTERN = Pattern.compile("^Bearer\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    private final TokenProvider tokenProvider;
    private final TokenBlacklistPort tokenBlacklistPort;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        final String p = request.getRequestURI();
        return p.startsWith("/api/v1/auth/login")
                || p.startsWith("/api/v1/auth/register")
                || p.startsWith("/api/v1/auth/activate")
                || p.startsWith("/api/v1/auth/resend-otp")
                || p.startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain)
            throws ServletException, IOException {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Authentication already present, skipping JWT validation.");
                }
                chain.doFilter(request, response);
                return;
            }

            final String token = extractBearerToken(request);
            if (token == null || token.isBlank()) {
                if (log.isDebugEnabled()) {
                    log.debug("No Bearer token found in Authorization header.");
                }
                chain.doFilter(request, response);
                return;
            }

            final Optional<AuthenticatedUser> validated = tokenProvider.validate(token);
            if (validated.isPresent()) {
                Optional<String> jti = tokenProvider.extractJti(token);
                if (jti.isPresent() && tokenBlacklistPort.isBlacklisted(jti.get())) {
                    log.warn("Rejected blacklisted token jti={}", jti.get());
                    respondUnauthorized(response);
                    return;
                }
                setAuthentication(validated.get(), request);
            } else {
                log.warn("JWT validation failed: token is invalid, expired, or has incorrect claims.");
            }

        } catch (Exception e) {
            log.warn("JWT parsing/validation error: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest req) {
        String h = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (h == null)
            return null;

        h = h.trim();
        if (h.length() >= BEARER_PREFIX.length() &&
                h.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return h.substring(BEARER_PREFIX.length()).trim();
        }

        Matcher m = BEARER_PATTERN.matcher(h);
        if (m.matches()) {
            return m.group(1).trim();
        }
        return null;
    }

    private void setAuthentication(AuthenticatedUser user, HttpServletRequest request) {
        Collection<GrantedAuthority> authorities = AuthorityMapper.mapToAuthorities(user.roles(), user.scopes());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (log.isDebugEnabled()) {
            log.debug("Authenticated user '{}' with authorities: {}",
                    user.username(),
                    authorities.stream().map(GrantedAuthority::getAuthority).toList());
        }
    }

    private void respondUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.getWriter().write("""
                    {"status":401,"error":"Unauthorized","message":"Invalid or expired token"}
                """);
    }
}

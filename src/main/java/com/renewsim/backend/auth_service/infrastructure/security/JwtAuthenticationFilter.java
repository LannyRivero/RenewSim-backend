package com.renewsim.backend.auth_service.infrastructure.security;

import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String p = request.getRequestURI();
        return p.startsWith("/api/v1/auth/login")
                || p.startsWith("/api/v1/auth/register")
                || p.startsWith("/actuator/health")
                || p.startsWith("/actuator/info");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Authentication already present, skipping JWT validation.");
                }
                return;
            }

            final String token = extractBearerToken(request);
            if (token == null || token.isBlank()) {
                if (log.isDebugEnabled()) {
                    log.debug("No Bearer token found in Authorization header.");
                }
                return;
            }

            final Optional<AuthenticatedUser> validated = tokenProvider.validate(token);
            if (validated.isPresent()) {
                setAuthentication(validated.get(), request);
            } else {
                log.warn("JWT validation failed: token is invalid, expired, or has incorrect claims.");
            }

        } catch (Exception e) {
            log.warn("JWT parsing/validation error: {}", e.getMessage());
        } finally {
            chain.doFilter(request, response);
        }
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
}

package com.renewsim.backend.auth_service.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.config.SecurityRateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimitingFilterUnitTest {

    @Test
    @DisplayName("IP_USER: does not consume body when buffer is empty (buildKey should not break request body)")
    void ipUser_doesNotConsumeEmptyBody() throws ServletException, IOException {
        SecurityRateLimitProperties props = new SecurityRateLimitProperties();
        props.setEnabled(true);
        props.setStrategy(SecurityRateLimitProperties.Strategy.IP_USER);
        props.setMaxAttempts(5);
        props.setWindow(Duration.ofSeconds(3));
        props.setRetryAfter(Duration.ofSeconds(3));
        props.setLoginPath("/api/v1/auth/login");

        LoginRateLimiter limiter = new LoginRateLimiter(
                Math.toIntExact(props.getWindow().toSeconds()),
                props.getMaxAttempts());

        LoginRateLimitingFilter filter = new LoginRateLimitingFilter(
                limiter,
                new ObjectMapper(),
                props);

        MockHttpServletRequest raw = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        raw.setRemoteAddr("127.0.0.1");
        raw.setContentType("application/json");
        raw.setContent(new byte[0]); // vacío

        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(raw);
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        filter.doFilter(wrapped, res, chain);

        assertThat(wrapped.getContentAsByteArray()).isEmpty();
        assertThat(res.getStatus()).isIn(0, 200);
    }
}

package com.renewsim.backend.config;

import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * Test configuration that neutralizes login rate-limiting infrastructure for
 * Spring Boot tests that focus on web/controller behavior.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public LoginRateLimitingFilter loginRateLimitingFilter() {
        return mock(LoginRateLimitingFilter.class);
    }
}

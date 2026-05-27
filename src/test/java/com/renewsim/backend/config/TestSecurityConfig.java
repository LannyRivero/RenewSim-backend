package com.renewsim.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.infrastructure.config.SecurityRateLimitProperties;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimiter;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * Test configuration to provide mock beans for missing dependencies.
 * 
 * TECHNICAL DEBT: LoginRateLimiter implementation is missing in production code.
 * This mock allows tests to pass while the proper implementation is pending.
 * 
 * TODO Task 1.2/1.3: Implement proper LoginRateLimiter with:
 * - Interface definition
 * - Production implementation (e.g., Redis-based or in-memory)
 * - Conditional configuration (@ConditionalOnProperty)
 * - Unit tests
 * 
 * @see com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Provides a mock LoginRateLimiter bean for tests.
     * Uses @Primary to ensure this bean is selected over any other candidates.
     */
    @Bean(name = "testLoginRateLimiter")
    @Primary
    public LoginRateLimiter loginRateLimiter() {
        return mock(LoginRateLimiter.class);
    }

    @Bean
    @Primary
    public LoginRateLimitingFilter loginRateLimitingFilter(ObjectMapper objectMapper) {
        SecurityRateLimitProperties props = new SecurityRateLimitProperties();
        props.setEnabled(false);
        return new LoginRateLimitingFilter(new LoginRateLimiter(60, 1000), objectMapper, props);
    }
}

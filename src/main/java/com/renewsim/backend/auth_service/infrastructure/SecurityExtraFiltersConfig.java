package com.renewsim.backend.auth_service.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.config.SecurityRateLimitProperties;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimiter;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityExtraFiltersConfig {

    @Bean
    public LoginRateLimiter loginRateLimiter(SecurityRateLimitProperties props) {
        int windowSeconds = Math.toIntExact(props.getWindow().toSeconds());
        int maxAttempts = props.getMaxAttempts();
        return new LoginRateLimiter(windowSeconds, maxAttempts);
    }

    @Bean
    public LoginRateLimitingFilter loginRateLimitingFilter(SecurityRateLimitProperties props,
                                                           ObjectMapper objectMapper,
                                                           LoginRateLimiter limiter) {
        return new LoginRateLimitingFilter(props, objectMapper, limiter);
    }
}


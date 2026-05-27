package com.renewsim.backend.auth_service.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;

import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public LoginRateLimitingFilter loginRateLimitingFilter() {
        return Mockito.mock(LoginRateLimitingFilter.class);
    }
}

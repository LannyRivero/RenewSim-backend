package com.renewsim.backend.auth_service.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;

@Configuration
public class TestSecurityConfig {

    @Bean
    @Primary
    public LoginRateLimitingFilter loginRateLimitingFilter() {
        return Mockito.mock(LoginRateLimitingFilter.class);
    }
}
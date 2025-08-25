package com.renewsim.backend.auth_service.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.config.SecurityRateLimitProperties;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityRateLimitProperties.class)
public class SecurityExtraFiltersConfig {

    @Bean
    @ConditionalOnProperty(prefix = "auth.rate-limiting", name = "enabled", havingValue = "true", matchIfMissing = true)
    public LoginRateLimitingFilter loginRateLimitingFilter(SecurityRateLimitProperties props, ObjectMapper objectMapper) {
        return new LoginRateLimitingFilter(props, objectMapper);
    }    
}


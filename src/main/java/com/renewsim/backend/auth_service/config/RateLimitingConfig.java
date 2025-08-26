package com.renewsim.backend.auth_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SecurityRateLimitProperties.class)
public class RateLimitingConfig {
    // Empty on purpose: only enables @ConfigurationProperties bean

}

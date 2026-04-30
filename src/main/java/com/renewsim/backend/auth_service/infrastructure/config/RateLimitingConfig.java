package com.renewsim.backend.auth_service.infrastructure.config;

import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SecurityRateLimitProperties.class)
public class RateLimitingConfig {

    @Bean
    @ConditionalOnProperty(prefix = "security.rate-limiting", name = "enabled", havingValue = "true", matchIfMissing = true)
    public LoginRateLimiter loginRateLimiter(SecurityRateLimitProperties p) {
        LoginRateLimiter.Strategy strategy = (p.getStrategy() == SecurityRateLimitProperties.Strategy.IP_USER)
                ? LoginRateLimiter.Strategy.IP_USER
                : LoginRateLimiter.Strategy.IP;
        return new LoginRateLimiter(p.getWindowSeconds(), p.getMaxAttempts(), strategy);
    }
}
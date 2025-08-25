package com.renewsim.backend.auth_service.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.rate-limiting")
public class SecurityRateLimitProperties {

    public enum Strategy { IP, IP_USER }

    private boolean enabled = true;

    @NotNull
    private Strategy strategy = Strategy.IP;

    @Min(1)
    private int maxAttempts = 5;

    @Min(1)
    private int windowSeconds = 60;

    @Min(0)
    private int retryAfterSeconds = 60;

    @NotNull
    private String loginPath = "/api/v1/auth/login";
}


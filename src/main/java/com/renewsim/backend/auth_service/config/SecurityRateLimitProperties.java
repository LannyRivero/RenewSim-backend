package com.renewsim.backend.auth_service.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

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

    @NotNull
    private Duration window = Duration.ofSeconds(60);

    @NotNull
    private Duration retryAfter = Duration.ofSeconds(60);

    @NotBlank
    @Pattern(regexp = "^/.*", message = "loginPath must start with '/'")
    private String loginPath = "/api/v1/auth/login";
}


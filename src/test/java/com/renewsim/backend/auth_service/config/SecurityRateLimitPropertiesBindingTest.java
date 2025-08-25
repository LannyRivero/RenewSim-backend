package com.renewsim.backend.auth_service.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityRateLimitPropertiesBindingTest {

    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withPropertyValues(
                    "security.rate-limiting.enabled=true",
                    "security.rate-limiting.strategy=IP_USER",
                    "security.rate-limiting.window-seconds=60",
                    "security.rate-limiting.max-attempts=5",
                    "security.rate-limiting.login-path=/api/v1/auth/login"
            )
            .withUserConfiguration(BindingConfig.class);

    @EnableConfigurationProperties(SecurityRateLimitProperties.class)
    static class BindingConfig {}

    @Test
    @DisplayName("bind SecurityRateLimitProperties from application context")
    void bindProperties() {
        context.run(ctx -> {
            var props = ctx.getBean(SecurityRateLimitProperties.class);
            assertThat(props.isEnabled()).isTrue();
            assertThat(props.getStrategy().name()).isEqualTo("IP_USER");
            assertThat(props.getWindowSeconds()).isEqualTo(60);
            assertThat(props.getMaxAttempts()).isEqualTo(5);
            assertThat(props.getLoginPath()).isEqualTo("/api/v1/auth/login");
        });
    }
}

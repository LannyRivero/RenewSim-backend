package com.renewsim.backend.auth_service.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.renewsim.backend.auth_service.infrastructure.config.SecurityJwtProperties;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TestConfig.class));

    @Configuration
    static class TestConfig {
        @Bean
        SecurityJwtProperties securityJwtProperties() {
            return new SecurityJwtProperties(
                    "renewsim-auth",
                    "renewsim-app",
                    null,  
                    null,  
                    3600L,
                    0L,
                    0L,
                    3600L

            );
        }

        @Bean
        JwtTokenProvider jwtTokenProvider(SecurityJwtProperties props) {
            return new JwtTokenProvider(props, null);
        }
    }

    @Test
    @DisplayName("Spring context should fail to start without JWT secret")
    void contextFailsWithoutSecret() {
        contextRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasMessageContaining("No JWT secret configured");
        });
    }
}


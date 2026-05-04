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
                    "zAVmvhHflK1UXkKFMhSyE0SFycA5KDgv9rsjn/oHPDIhCS/40msmI7BVMY7kiwH13ewupe6GWShM75XnYimtaQ==",
                    null,
                    null,
                    3600L,
                    0L,
                    0L,
                    3600L,
                    604800L
            );
        }

        @Bean
        JwtClaimsExtractor jwtClaimsExtractor() {
            return new JwtClaimsExtractor();
        }

        @Bean
        JwtTokenProvider jwtTokenProvider(SecurityJwtProperties props, JwtClaimsExtractor claimsExtractor) {
            return new JwtTokenProvider(props, java.time.Clock.systemUTC(), claimsExtractor);
        }
    }

    @Test
    @DisplayName("Spring context should start with valid JWT secret")
    void contextStartsWithSecret() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(JwtTokenProvider.class);
        });
    }
}


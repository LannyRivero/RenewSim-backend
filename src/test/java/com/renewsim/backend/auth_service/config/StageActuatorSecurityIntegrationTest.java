package com.renewsim.backend.auth_service.config;

import com.renewsim.backend.auth_service.application.port.out.TokenBlacklistPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.infrastructure.config.ActuatorSecurityConfigProd;
import com.renewsim.backend.auth_service.infrastructure.security.JwtAuthenticationFilter;
import com.renewsim.backend.auth_service.infrastructure.security.SecurityHeadersFilter;
import com.renewsim.backend.shared.observability.CorrelationIdFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = StageActuatorSecurityIntegrationTest.TestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("stage")
class StageActuatorSecurityIntegrationTest {

    private static final String VALID_SCOPED_BEARER = "stage-actuator-token";

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("stage actuator health remains public")
    void actuatorHealthIsPublicInStage() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("stage actuator prometheus is not public")
    void actuatorPrometheusIsNotPublicInStage() throws Exception {
        mvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_actuator:read")
    @DisplayName("stage actuator prometheus allows scoped identity")
    void actuatorPrometheusAllowsScopedIdentity() throws Exception {
        mvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("stage actuator info is not public")
    void actuatorInfoIsNotPublicInStage() throws Exception {
        mvc.perform(get("/actuator/info"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("stage actuator info allows scoped bearer token")
    void actuatorInfoAllowsScopedBearerToken() throws Exception {
        mvc.perform(get("/actuator/info")
                .header("Authorization", "Bearer " + VALID_SCOPED_BEARER))
                .andExpect(status().isOk());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class
    })
    @Import({ ActuatorSecurityConfigProd.class, SecurityHeadersFilter.class, CorrelationIdFilter.class,
            DummyActuatorController.class })
    static class TestApp {

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(TokenProvider tokenProvider, TokenBlacklistPort tokenBlacklistPort) {
            return new JwtAuthenticationFilter(tokenProvider, tokenBlacklistPort);
        }

        @Bean
        TokenProvider tokenProvider() {
            return new TokenProvider() {
                @Override
                public String generate(AuthenticatedUser user) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String generate(AuthenticatedUser user, long expirationSeconds) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Optional<AuthenticatedUser> validate(String token) {
                    if (VALID_SCOPED_BEARER.equals(token)) {
                        return Optional.of(AuthenticatedUser.of("ops-service", Set.of("SERVICE_AUTH"), Set.of("actuator:read")));
                    }
                    return Optional.empty();
                }

                @Override
                public long expiresInSeconds() {
                    return 0;
                }

                @Override
                public long refreshExpiresInSeconds() {
                    return 0;
                }

                @Override
                public Optional<String> extractJti(String token) {
                    return Optional.empty();
                }

                @Override
                public Optional<Long> extractExpirationEpochSeconds(String token) {
                    return Optional.empty();
                }

                @Override
                public String generateServiceToken(String serviceName, Set<String> scopes) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Bean
        TokenBlacklistPort tokenBlacklistPort() {
            return new TokenBlacklistPort() {
                @Override
                public void blacklist(String jti, Long userId, long expiresAt) {
                }

                @Override
                public boolean isBlacklisted(String jti) {
                    return false;
                }
            };
        }
    }

    @RestController
    static class DummyActuatorController {

        @GetMapping("/actuator/health")
        String health() {
            return "ok";
        }

        @GetMapping("/actuator/prometheus")
        String prometheus() {
            return "metrics";
        }

        @GetMapping("/actuator/info")
        String info() {
            return "info";
        }
    }
}

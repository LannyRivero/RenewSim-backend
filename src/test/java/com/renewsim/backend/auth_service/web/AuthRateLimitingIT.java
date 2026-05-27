package com.renewsim.backend.auth_service.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.infrastructure.config.SecurityRateLimitProperties;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimiter;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AuthRateLimitingIT.RateLimitingTestConfig.class)
@TestPropertySource(properties = {
    "security.rate-limiting.enabled=true",
    "security.rate-limiting.strategy=IP",
    "security.rate-limiting.max-attempts=2",
    "security.rate-limiting.window=30s",
    "security.rate-limiting.retry-after=30s",
    "security.rate-limiting.login-path=/api/v1/auth/login"
})
class AuthRateLimitingIT {

    @TestConfiguration
    static class RateLimitingTestConfig {
        @Bean(name = "authRateLimitingFilter")
        @Primary
        LoginRateLimitingFilter authRateLimitingFilter(ObjectMapper objectMapper, SecurityRateLimitProperties p) {
            LoginRateLimiter.Strategy strategy = (p.getStrategy() == SecurityRateLimitProperties.Strategy.IP_USER)
                    ? LoginRateLimiter.Strategy.IP_USER
                    : LoginRateLimiter.Strategy.IP;
            return new LoginRateLimitingFilter(
                    new LoginRateLimiter(p.getWindowSeconds(), p.getMaxAttempts(), strategy),
                    objectMapper,
                    p);
        }
    }

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    SecurityRateLimitProperties rateProps;

    static class LoginDto {
        public String email;
        public String password;

        LoginDto(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    @Test
    @DisplayName("Exceed attempts -> 429 with Retry-After")
    void exceedAttemptsShouldReturn429AndRetryAfter() throws Exception {
        final String loginPath = rateProps.getLoginPath();
        final String randomEmail = "rate_" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com";
        final String body = objectMapper.writeValueAsString(
            new LoginDto(randomEmail, "WrongPassword123")
        );

        for (int i = 0; i < rateProps.getMaxAttempts() - 1; i++) {
            mvc.perform(post(loginPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isUnauthorized());
        }

        mvc.perform(post(loginPath)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized());

        var res = mvc.perform(post(loginPath)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andReturn();

        String retryAfter = res.getResponse().getHeader("Retry-After");
        assertThat(retryAfter).isNotBlank();
    }
}

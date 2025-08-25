package com.renewsim.backend.auth_service.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.config.SecurityRateLimitProperties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthRateLimitingITTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    SecurityRateLimitProperties rateProps;

    static class LoginDto {
        public String username;
        public String password;

        LoginDto(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    @Test
    @DisplayName("Exceed attempts → 429 with Retry-After")
    void exceedAttemptsShouldReturn429AndRetryAfter() throws Exception {
        final String loginPath = rateProps.getLoginPath(); // e.g. /api/v1/auth/login
        final String body = objectMapper.writeValueAsString(
                new LoginDto("user@example.com", "WrongPassword123") // válido pero incorrecto
        );

        for (int i = 0; i < rateProps.getMaxAttempts() - 1; i++) {
            mvc.perform(post(loginPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isUnauthorized()); // 401
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

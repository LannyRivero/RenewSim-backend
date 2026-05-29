package com.renewsim.backend.auth_service.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.application.port.in.LoginUseCase;
import com.renewsim.backend.auth_service.application.result.LoginResult;
import com.renewsim.backend.auth_service.web.dto.LoginRequestDTO;
import com.renewsim.backend.config.TestSecurityConfig;
import com.renewsim.backend.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AuthHeadersIntegrationTest {

        @Autowired
        MockMvc mockMvc;
        @MockitoBean
        LoginUseCase loginUseCase;
        private final ObjectMapper om = new ObjectMapper();

        @Test
        @DisplayName("Auth /login returns security and no-cache headers on 200")
        void loginOk_hasSecurityAndNoCacheHeaders() throws Exception {
                Mockito.when(loginUseCase.execute(any()))
                                .thenReturn(new LoginResult(
                                                "token-123",
                                                "refresh-123",
                                                "Bearer",
                                                3600,
                                                1L,
                                                "user@test.com",
                                                Set.of("USER")));

                LoginRequestDTO req = new LoginRequestDTO("user@test.com", "StrongPass123!");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(req)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                                .andExpect(header().string("X-Frame-Options", "DENY"))
                                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                                .andExpect(header().exists("Content-Security-Policy"))
                                .andExpect(header().string("Cache-Control", "no-store"))
                                .andExpect(header().string("Pragma", "no-cache"))
                                .andExpect(header().string("Expires",
                                                anyOf(equalTo("0"), equalTo("Thu, 01 Jan 1970 00:00:00 GMT"))));
        }

        @Test
        @DisplayName("Auth /login returns security and no-cache headers on 401")
        void loginUnauthorized_hasSecurityAndNoCacheHeaders() throws Exception {
                Mockito.when(loginUseCase.execute(any()))
                                .thenThrow(new UnauthorizedException("AUTH_INVALID_CREDENTIALS: Invalid credentials"));

                LoginRequestDTO req = new LoginRequestDTO("user@test.com", "WrongPass123!");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(req)))
                                .andDo(print())
                                .andExpect(status().isUnauthorized())
                                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                                .andExpect(header().string("X-Frame-Options", "DENY"))
                                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                                .andExpect(header().exists("Content-Security-Policy"))
                                .andExpect(header().string("Cache-Control", "no-store"))
                                .andExpect(header().string("Pragma", "no-cache"))
                                .andExpect(header().string("Expires",
                                                anyOf(equalTo("0"), equalTo("Thu, 01 Jan 1970 00:00:00 GMT"))));
        }
}

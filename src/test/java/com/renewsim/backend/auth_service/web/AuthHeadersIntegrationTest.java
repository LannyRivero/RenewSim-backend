package com.renewsim.backend.auth_service.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.application.port.in.AuthUseCase;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthHeadersIntegrationTest {

        @Autowired
        MockMvc mockMvc;
        @MockBean
        AuthUseCase authUseCase;
        private final ObjectMapper om = new ObjectMapper();

        @Test
        @DisplayName("Auth /login returns security and no-cache headers on 200")
        void loginOk_hasSecurityAndNoCacheHeaders() throws Exception {
                Mockito.when(authUseCase.login(any()))
                                .thenReturn(AuthResponseDTO.builder()
                                                .token("token-123")
                                                .username("user@test.com")
                                                .build());

                AuthRequestDTO req = new AuthRequestDTO("user@test.com", "StrongPass123!");

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
                Mockito.when(authUseCase.login(any()))
                                .thenThrow(new org.springframework.security.authentication.BadCredentialsException(
                                                "invalid"));

                AuthRequestDTO req = new AuthRequestDTO("user@test.com", "WrongPass123!");

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

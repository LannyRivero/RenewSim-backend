package com.renewsim.backend.auth_service.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/auth/login → 200 OK con credenciales correctas")
    void login_ok() throws Exception {
        AuthRequestDTO req = new AuthRequestDTO("john", "secret");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.scopes").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login → 401 Unauthorized con password incorrecto")
    void login_invalidPassword() throws Exception {
        AuthRequestDTO req = new AuthRequestDTO("john", "wrong");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register → 409 Conflict en username duplicado")
    void register_conflict() throws Exception {
        AuthRequestDTO req = new AuthRequestDTO("john", "secret");

        // Primer registro debería pasar
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Segundo registro con mismo usuario → 409
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH_USERNAME_CONFLICT"))
                .andExpect(jsonPath("$.message").exists());
    }
}


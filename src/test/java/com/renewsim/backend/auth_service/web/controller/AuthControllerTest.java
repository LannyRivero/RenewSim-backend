package com.renewsim.backend.auth_service.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.application.port.in.AuthUseCase;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerTest {

    private MockMvc mvc;
    private ObjectMapper mapper;
    private AuthUseCase authUseCase;

    @BeforeEach
    void setUp() {
        authUseCase = Mockito.mock(AuthUseCase.class);

        final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mvc = standaloneSetup(new AuthController(authUseCase))
                .setValidator(validator)
                .build();

        mapper = new ObjectMapper();
    }

    @Test
    @DisplayName("login → should return 200, no-store cache header and response body")
    void testShouldLoginAndReturnNoStoreHeader() throws Exception {
        final AuthRequestDTO req = new AuthRequestDTO("john", "secret");

        final AuthResponseDTO res = AuthResponseDTO.builder()
                .username("john")
                .token("jwt-token")
                .tokenType("Bearer")
                .expiresAt(Instant.now().plusSeconds(3600))
                .roles(Set.of("USER"))
                .scopes(Set.of("read"))
                .build();

        when(authUseCase.login(any())).thenReturn(res);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.roles[0]").exists())
                .andExpect(jsonPath("$.scopes[0]").exists());

        verify(authUseCase).login(any());
    }

    @Test
    @DisplayName("register → should return 201, no-store cache header and response body")
    void testShouldRegisterAndReturnCreatedWithNoStoreHeader() throws Exception {
        final AuthRequestDTO req = new AuthRequestDTO("mary", "StrongPass_1");

        final AuthResponseDTO res = AuthResponseDTO.builder()
                .username("mary")
                .token("jwt-created")
                .tokenType("Bearer")
                .expiresAt(Instant.now().plusSeconds(3600))
                .roles(Set.of("USER"))
                .scopes(Set.of("read"))
                .build();

        when(authUseCase.register(any())).thenReturn(res);

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
                .andExpect(jsonPath("$.username").value("mary"))
                .andExpect(jsonPath("$.token").value("jwt-created"));

        verify(authUseCase).register(any());
    }

    @Test
    @DisplayName("login → should return 400 when payload is invalid (@Valid)")
    void testShouldReturnBadRequestWhenInvalidBody() throws Exception {
        final String invalidJson = """
            {"username":null,"password":null}
        """;

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authUseCase);
    }

    @Test
    @DisplayName("register → should return 400 when registration payload is invalid (@Valid)")
    void testShouldReturnBadRequestWhenInvalidRegistrationBody() throws Exception {
        final String invalidJson = """
            {"username":null,"password":null,"email":null}
        """;

        mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON) 
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authUseCase);
    }
}




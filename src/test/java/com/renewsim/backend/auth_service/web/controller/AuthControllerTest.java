package com.renewsim.backend.auth_service.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.application.port.in.*;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterRequestDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterResponseDTO;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.Set;

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
                AuthController controller = new AuthController(
                                authUseCase,
                                Mockito.mock(LoginStep1UseCase.class),
                                Mockito.mock(LoginStep2UseCase.class),
                                Mockito.mock(ActivateAccountUseCase.class),
                                Mockito.mock(ResendOtpUseCase.class),
                                Mockito.mock(LogoutUseCase.class),
                                Mockito.mock(RefreshTokenUseCase.class));

                final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
                validator.afterPropertiesSet();

                mvc = standaloneSetup(controller)
                                .setValidator(validator)
                                .build();

                mapper = new ObjectMapper();
        }

        @Test
        @DisplayName("login -> devuelve 200 y response body")
        void login_validCredentials_returns200() throws Exception {
                AuthRequestDTO req = new AuthRequestDTO("john", "secret");
                AuthResponseDTO res = AuthResponseDTO.builder()
                                .username("john")
                                .token("jwt-token")
                                .tokenType("Bearer")
                                .expiresAt(Instant.now().plusSeconds(3600))
                                .roles(Set.of("USER"))
                                .scopes(Set.of("read:simulations"))
                                .build();

                when(authUseCase.login(any())).thenReturn(res);

                mvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.username").value("john"))
                                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                                .andExpect(jsonPath("$.data.roles[0]").exists())
                                .andExpect(jsonPath("$.data.scopes[0]").exists());

                verify(authUseCase).login(any());
        }

        @Test
        @DisplayName("register -> devuelve 201 y RegisterResponseDTO")
        void register_validRequest_returns201() throws Exception {
                RegisterRequestDTO req = new RegisterRequestDTO(
                                "mary@example.com", "StrongPass_1!", "Mary Doe");

                RegisterResponseDTO res = new RegisterResponseDTO(
                                1L,
                                "mary@example.com",
                                "Mary Doe",
                                UserStatus.INACTIVE,
                                "User registered successfully. Please check your email to activate your account.");

                when(authUseCase.register(any())).thenReturn(res);

                mvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.email").value("mary@example.com"))
                                .andExpect(jsonPath("$.data.fullName").value("Mary Doe"))
                                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                                .andExpect(jsonPath("$.data.message").exists());

                verify(authUseCase).register(any());
        }

        @Test
        @DisplayName("login -> devuelve 400 cuando el payload es inválido (@Valid)")
        void login_invalidPayload_returns400() throws Exception {
                mvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":null,\"password\":null}"))
                                .andExpect(status().isBadRequest());

                verifyNoInteractions(authUseCase);
        }

        @Test
        @DisplayName("register -> devuelve 400 cuando el payload es inválido (@Valid)")
        void register_invalidPayload_returns400() throws Exception {
                mvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":null,\"password\":null,\"fullName\":null}"))
                                .andExpect(status().isBadRequest());

                verifyNoInteractions(authUseCase);
        }
}
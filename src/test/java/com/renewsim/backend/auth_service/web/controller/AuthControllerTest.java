package com.renewsim.backend.auth_service.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.application.port.in.*;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterRequestDTO;

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
    @DisplayName("login -> should return 200 and response body")
    void testShouldLoginAndReturnOk() throws Exception {
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
                .andExpect(jsonPath("$.data.username").value("john"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.roles[0]").exists())
                .andExpect(jsonPath("$.data.scopes[0]").exists());

        verify(authUseCase).login(any());
    }

    @Test
    @DisplayName("register -> should return 201 and response body")
    void testShouldRegisterAndReturnCreated() throws Exception {
        final RegisterRequestDTO req = new RegisterRequestDTO("mary", "StrongPass_1", "mary@example.com");

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
                .andExpect(jsonPath("$.data.username").value("mary"))
                .andExpect(jsonPath("$.data.token").value("jwt-created"));

        verify(authUseCase).register(any());
    }

    @Test
    @DisplayName("login -> should return 400 when payload is invalid (@Valid)")
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
    @DisplayName("register -> should return 400 when registration payload is invalid (@Valid)")
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
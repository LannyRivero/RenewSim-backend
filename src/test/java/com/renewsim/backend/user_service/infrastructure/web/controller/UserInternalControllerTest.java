package com.renewsim.backend.user_service.infrastructure.web.controller;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.infrastructure.security.JwtTokenProvider;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;
import com.renewsim.backend.config.TestSecurityConfig;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.user_service.application.port.in.GetUserUseCase;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("UserInternalController")
class UserInternalControllerTest {

    private static final String USER_TOKEN = "user-token";
    private static final String SERVICE_TOKEN = "service-token";
    private static final String PASSWORD_HASH = new BCryptPasswordEncoder(12).encode("StrongPass1");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private LoginRateLimitingFilter loginRateLimitingFilter;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            inv.getArgument(2, FilterChain.class).doFilter(
                    inv.getArgument(0, ServletRequest.class),
                    inv.getArgument(1, ServletResponse.class));
            return null;
        }).when(loginRateLimitingFilter).doFilter(any(), any(), any());

        when(jwtTokenProvider.validate(USER_TOKEN))
                .thenReturn(Optional.of(new AuthenticatedUser("user", Set.of("USER"), Set.of())));
        when(jwtTokenProvider.validate(SERVICE_TOKEN))
                .thenReturn(Optional.of(new AuthenticatedUser("auth-service", Set.of("SERVICE_AUTH"), Set.of())));
    }

    @Test
    @DisplayName("credential and snapshot routes deny USER and return passwordHash to SERVICE_AUTH")
    void internalRoutes_requireServiceAuthAndExposeCredentialContract() throws Exception {
        User user = User.reconstitute(
                7L, "user@renewsim.com", PASSWORD_HASH, "RenewSim User", null,
                UserStatus.ACTIVE, Set.of(RoleName.USER),
                LocalDateTime.parse("2026-01-01T00:00:00"),
                LocalDateTime.parse("2026-01-01T00:00:00"), true,
                LocalDateTime.parse("2026-01-01T00:00:00"));
        when(getUserUseCase.getDomainUserByUsernameOrEmail("user", null)).thenReturn(user);
        when(getUserUseCase.getDomainUserById(7L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/internal/credentials").param("username", "user")
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_TOKEN)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/users/7/snapshot")
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_TOKEN)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/users/internal/credentials").param("username", "user")
                        .header(HttpHeaders.AUTHORIZATION, bearer(SERVICE_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.passwordHash").value(PASSWORD_HASH));
        mockMvc.perform(get("/api/v1/users/7/snapshot")
                        .header(HttpHeaders.AUTHORIZATION, bearer(SERVICE_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.passwordHash").value(PASSWORD_HASH));
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}

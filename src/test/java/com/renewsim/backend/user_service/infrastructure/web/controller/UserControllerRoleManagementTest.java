package com.renewsim.backend.user_service.infrastructure.web.controller;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.infrastructure.security.JwtTokenProvider;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;
import com.renewsim.backend.config.TestSecurityConfig;
import com.renewsim.backend.shared.exception.ResourceNotFoundException;
import com.renewsim.backend.user_service.application.port.in.ActivateUserUseCase;
import com.renewsim.backend.user_service.application.port.in.AssignUserRoleUseCase;
import com.renewsim.backend.user_service.application.port.in.ChangeMyPasswordUseCase;
import com.renewsim.backend.user_service.application.port.in.CreateUserUseCase;
import com.renewsim.backend.user_service.application.port.in.DeleteUserUseCase;
import com.renewsim.backend.user_service.application.port.in.ExistsUserUseCase;
import com.renewsim.backend.user_service.application.port.in.GetMyProfileUseCase;
import com.renewsim.backend.user_service.application.port.in.GetUserUseCase;
import com.renewsim.backend.user_service.application.port.in.ListUsersUseCase;
import com.renewsim.backend.user_service.application.port.in.RemoveUserRoleUseCase;
import com.renewsim.backend.user_service.application.port.in.UpdateMyProfileUseCase;
import com.renewsim.backend.user_service.application.port.in.UpdateUserRolesUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("UserController role management")
class UserControllerRoleManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private LoginRateLimitingFilter loginRateLimitingFilter;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private ExistsUserUseCase existsUserUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @MockitoBean
    private ListUsersUseCase listUsersUseCase;

    @MockitoBean
    private UpdateUserRolesUseCase updateUserRolesUseCase;

    @MockitoBean
    private AssignUserRoleUseCase assignUserRoleUseCase;

    @MockitoBean
    private RemoveUserRoleUseCase removeUserRoleUseCase;

    @MockitoBean
    private GetMyProfileUseCase getMyProfileUseCase;

    @MockitoBean
    private UpdateMyProfileUseCase updateMyProfileUseCase;

    @MockitoBean
    private ChangeMyPasswordUseCase changeMyPasswordUseCase;

    @MockitoBean
    private ActivateUserUseCase activateUserUseCase;

    @MockitoBean
    private DeleteUserUseCase deleteUserUseCase;

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String USER_TOKEN = "user-token";

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            inv.getArgument(2, FilterChain.class).doFilter(
                    inv.getArgument(0, ServletRequest.class),
                    inv.getArgument(1, ServletResponse.class));
            return null;
        }).when(loginRateLimitingFilter).doFilter(any(), any(), any());

        when(jwtTokenProvider.validate(ADMIN_TOKEN))
                .thenReturn(Optional.of(new AuthenticatedUser(
                        "admin@renewsim.com", Set.of("ADMIN"), Set.of())));

        when(jwtTokenProvider.validate(USER_TOKEN))
                .thenReturn(Optional.of(new AuthenticatedUser(
                        "user@renewsim.com", Set.of("USER"), Set.of())));
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Nested
    @DisplayName("POST /api/v1/users/{userId}/roles/{roleId}")
    class AssignRole {

        @Test
        @DisplayName("should return 200 when ADMIN assigns role")
        void assignRole_success() throws Exception {
            mockMvc.perform(post("/api/v1/users/7/roles/2")
                            .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN_TOKEN)))
                    .andExpect(status().isOk());

            verify(assignUserRoleUseCase).assignRole(7L, 2L);
        }

        @Test
        @DisplayName("should return 404 when user or role does not exist")
        void assignRole_notFound() throws Exception {
            doThrow(new ResourceNotFoundException("Role with id=2 not found"))
                    .when(assignUserRoleUseCase).assignRole(7L, 2L);

            mockMvc.perform(post("/api/v1/users/7/roles/2")
                            .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN_TOKEN)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 for USER role")
        void assignRole_forbidden() throws Exception {
            mockMvc.perform(post("/api/v1/users/7/roles/2")
                            .header(HttpHeaders.AUTHORIZATION, bearer(USER_TOKEN)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{userId}/roles/{roleId}")
    class RemoveRole {

        @Test
        @DisplayName("should return 200 when ADMIN removes role")
        void removeRole_success() throws Exception {
            mockMvc.perform(delete("/api/v1/users/7/roles/2")
                            .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN_TOKEN)))
                    .andExpect(status().isOk());

            verify(removeUserRoleUseCase).removeRole(7L, 2L);
        }

        @Test
        @DisplayName("should return 404 when assignment does not exist")
        void removeRole_notFound() throws Exception {
            doThrow(new ResourceNotFoundException("Role assignment not found"))
                    .when(removeUserRoleUseCase).removeRole(7L, 2L);

            mockMvc.perform(delete("/api/v1/users/7/roles/2")
                            .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN_TOKEN)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 for USER role")
        void removeRole_forbidden() throws Exception {
            mockMvc.perform(delete("/api/v1/users/7/roles/2")
                            .header(HttpHeaders.AUTHORIZATION, bearer(USER_TOKEN)))
                    .andExpect(status().isForbidden());
        }
    }
}

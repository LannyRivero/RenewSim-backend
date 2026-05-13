package com.renewsim.backend.role_service.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.infrastructure.security.JwtTokenProvider;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;
import com.renewsim.backend.config.TestSecurityConfig;
import com.renewsim.backend.role_service.application.port.in.*;
import com.renewsim.backend.role_service.application.result.*;
import com.renewsim.backend.role_service.web.dto.RoleCreateRequestDTO;
import com.renewsim.backend.role_service.web.dto.RoleDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("RoleController")
class RoleControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private LoginRateLimitingFilter loginRateLimitingFilter;
    @MockBean private CreateRoleUseCase createRoleUseCase;
    @MockBean private GetRolesUseCase getRolesUseCase;
    @MockBean private ExistsRoleUseCase existsRoleUseCase;
    @MockBean private AssignRoleUseCase assignRoleUseCase;
    @MockBean private DeleteRoleUseCase deleteRoleUseCase;
    @MockBean private ManageUserRolesUseCase manageUserRolesUseCase;

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String USER_TOKEN = "user-token";

    @BeforeEach
    void setUp() throws Exception {
        // Allow filter chain to pass through
        doAnswer(inv -> {
            inv.getArgument(0, ServletRequest.class);
            inv.getArgument(1, ServletResponse.class);
            inv.getArgument(2, FilterChain.class).doFilter(
                    inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(loginRateLimitingFilter).doFilter(any(), any(), any());

        // ADMIN token
        when(jwtTokenProvider.validate(ADMIN_TOKEN))
                .thenReturn(Optional.of(new AuthenticatedUser(
                        "admin@renewsim.com", Set.of("ADMIN"), Set.of())));

        // USER token
        when(jwtTokenProvider.validate(USER_TOKEN))
                .thenReturn(Optional.of(new AuthenticatedUser(
                        "user@renewsim.com", Set.of("USER"), Set.of())));
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    // ─────────────────────────────────────────────
    // POST /roles
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/v1/roles")
    class CreateRole {

        @Test
        @DisplayName("should return 201 when ADMIN creates role")
        void createRole_success() throws Exception {
            when(createRoleUseCase.createRole(any()))
                    .thenReturn(new RoleCreationResultDTO("ADMIN", "Role created successfully"));

            mockMvc.perform(post("/api/v1/roles")
                            .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN_TOKEN))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RoleCreateRequestDTO("ADMIN"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value("ADMIN"));
        }

        @Test
        @DisplayName("should return 400 when role name is blank")
        void createRole_validationError() throws Exception {
            mockMvc.perform(post("/api/v1/roles")
                            .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN_TOKEN))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"   \"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 403 for USER role")
        void createRole_forbidden() throws Exception {
            mockMvc.perform(post("/api/v1/roles")
                            .header(HttpHeaders.AUTHORIZATION, bearer(USER_TOKEN))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RoleCreateRequestDTO("USER"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void createRole_unauthorized() throws Exception {
            mockMvc.perform(post("/api/v1/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RoleCreateRequestDTO("ADMIN"))))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // GET /roles
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/roles")
    class GetRoles {

        @Test
        @DisplayName("should return roles list for USER")
        void getAllRoles_success() throws Exception {
            when(getRolesUseCase.getAll()).thenReturn(List.of(new RoleDTO(1L, "USER")));

            mockMvc.perform(get("/api/v1/roles")
                            .header(HttpHeaders.AUTHORIZATION, bearer(USER_TOKEN)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].name").value("USER"));
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void getAllRoles_unauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/roles"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // GET /roles/exists/{name}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/roles/exists/{name}")
    class ExistsRole {

        @Test
        @DisplayName("should return true when role exists")
        void existsRole_true() throws Exception {
            when(existsRoleUseCase.existsByName(any())).thenReturn(true);

            mockMvc.perform(get("/api/v1/roles/exists/ADMIN")
                            .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN_TOKEN)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("should return false when role does not exist")
        void existsRole_false() throws Exception {
            when(existsRoleUseCase.existsByName(any())).thenReturn(false);

            mockMvc.perform(get("/api/v1/roles/exists/UNKNOWN")
                            .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN_TOKEN)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));
        }
    }

    // ─────────────────────────────────────────────
    // PUT /roles/{roleId}/assign/{userId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/v1/roles/{roleId}/assign/{userId}")
    class AssignRole {

        @Test
        @DisplayName("should assign role successfully")
        void assignRole_success() throws Exception {
            when(assignRoleUseCase.assignRoleToUser(any()))
                    .thenReturn(new RoleAssignmentResultDTO(100L, "ADMIN", true, "assigned"));

            mockMvc.perform(put("/api/v1/roles/1/assign/100")
                            .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN_TOKEN)))
                    .andExpect(status().isOk());

            verify(assignRoleUseCase).assignRoleToUser(any());
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void assignRole_unauthorized() throws Exception {
            mockMvc.perform(put("/api/v1/roles/1/assign/100"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 for USER role")
        void assignRole_forbidden() throws Exception {
            mockMvc.perform(put("/api/v1/roles/1/assign/100")
                            .header(HttpHeaders.AUTHORIZATION, bearer(USER_TOKEN)))
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE /roles/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/v1/roles/{id}")
    class DeleteRole {

        @Test
        @DisplayName("should delete role successfully")
        void deleteRole_success() throws Exception {
            mockMvc.perform(delete("/api/v1/roles/1")
                            .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN_TOKEN)))
                    .andExpect(status().isOk());

            verify(deleteRoleUseCase).delete(1L);
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void deleteRole_unauthorized() throws Exception {
            mockMvc.perform(delete("/api/v1/roles/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 for USER role")
        void deleteRole_forbidden() throws Exception {
            mockMvc.perform(delete("/api/v1/roles/1")
                            .header(HttpHeaders.AUTHORIZATION, bearer(USER_TOKEN)))
                    .andExpect(status().isForbidden());
        }
    }
}

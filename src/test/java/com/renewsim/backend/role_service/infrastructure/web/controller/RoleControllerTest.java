package com.renewsim.backend.role_service.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.config.TestSecurityConfig;
import com.renewsim.backend.role_service.web.dto.RoleCreateRequestDTO;
import com.renewsim.backend.role_service.web.dto.RoleDTO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.renewsim.backend.role_service.application.command.AssignRoleCommand;
import com.renewsim.backend.role_service.application.port.in.*;
import com.renewsim.backend.role_service.application.result.RoleCreationResultDTO;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateRoleUseCase createRoleUseCase;
    @MockBean
    private GetRolesUseCase getRolesUseCase;
    @MockBean
    private AssignRoleUseCase assignRoleUseCase;
    @MockBean
    private DeleteRoleUseCase deleteRoleUseCase;

    // -------------------
    // CREATE ROLE
    // -------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /roles should create role when authorized")
    void createRole_success() throws Exception {
        RoleCreationResultDTO dto = new RoleCreationResultDTO( "ADMIN", "Role created successfully");
        when(createRoleUseCase.createRole(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RoleCreateRequestDTO("ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /roles should return 400 when role name is blank")
    void createRole_validationError() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RoleCreateRequestDTO("   "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /roles should return 403 for USER")
    void createRole_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RoleDTO(null, "USER"))))
                .andExpect(status().isForbidden());
    }

    // -------------------
    // GET ROLES
    // -------------------
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /roles should return roles for USER")
    void getAllRoles_success() throws Exception {
        when(getRolesUseCase.getAll()).thenReturn(List.of(new RoleDTO(1L, "USER")));

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("USER"));
    }

    // -------------------
    // EXISTS ROLE
    // -------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /roles/exists/{name} should return true if role exists")
    void existsRole_true() throws Exception {
        when(getRolesUseCase.getAll()).thenReturn(List.of(new RoleDTO(1L, "ADMIN")));

        mockMvc.perform(get("/api/v1/roles/exists/ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // -------------------
    // ASSIGN ROLE
    // -------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /roles/{roleId}/assign/{userId} should assign role")
    void assignRole_success() throws Exception {
        mockMvc.perform(put("/api/v1/roles/1/assign/100"))
                .andExpect(status().isNoContent());
        verify(assignRoleUseCase).assignRoleToUser(new AssignRoleCommand(1L, 100L, 1L));
    }

    // -------------------
    // DELETE ROLE
    // -------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /roles/{id} should delete role")
    void deleteRole_success() throws Exception {
        mockMvc.perform(delete("/api/v1/roles/1"))
                .andExpect(status().isNoContent());
        verify(deleteRoleUseCase).delete(1L);
    }

}

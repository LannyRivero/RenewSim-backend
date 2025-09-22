package com.renewsim.backend.role_service.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.role_service.dto.RoleCreateRequestDTO;
import com.renewsim.backend.role_service.dto.RoleDTO;
import com.renewsim.backend.shared.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.renewsim.backend.role_service.application.port.in.*;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class) 
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private CreateRoleUseCase createRoleUseCase;
    @MockBean private GetRolesUseCase getRolesUseCase;
    @MockBean private AssignRoleUseCase assignRoleUseCase;
    @MockBean private DeleteRoleUseCase deleteRoleUseCase;

    @Test
@WithMockUser(roles = "ADMIN")
@DisplayName("POST /roles should create role when authorized")
void createRole_success() throws Exception {
    RoleDTO dto = new RoleDTO(1L, "ADMIN");
    when(createRoleUseCase.create(any())).thenReturn(dto);

    mockMvc.perform(post("/api/v1/roles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new RoleCreateRequestDTO("ADMIN"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("ADMIN"));
}


    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /roles should return roles for USER")
    void getAllRoles_success() throws Exception {
        when(getRolesUseCase.getAll()).thenReturn(List.of(new RoleDTO(1L, "USER")));

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("USER"));
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
}


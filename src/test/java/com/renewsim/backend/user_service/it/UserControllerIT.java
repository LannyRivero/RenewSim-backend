package com.renewsim.backend.user_service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.user_service.dto.UserCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") 
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------
    // POST /users
    // ---------------------------
    @Test
    @DisplayName("should return 201 Created when user is created successfully")
    @WithMockUser(roles = "ADMIN")
    void testCreateUserReturns201() throws Exception {
        UserCreateRequest request = new UserCreateRequest("alice", "alice@mail.com", "StrongPass1");
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("should return 400 Bad Request when request body is invalid")
    @WithMockUser(roles = "ADMIN")
    void testCreateUserInvalidBodyReturns400() throws Exception {
        UserCreateRequest invalid = new UserCreateRequest("", "invalid", "123");
        String body = objectMapper.writeValueAsString(invalid);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    } 

    // ---------------------------
    // Seguridad
    // ---------------------------
    @Test
    @DisplayName("should return 401 Unauthorized when no token provided")
    void testUnauthorizedReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 1))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return 403 Forbidden when user has no write permission")
    @WithMockUser(roles = "USER")
    void testForbiddenOnCreateUserReturns403() throws Exception {
        UserCreateRequest request = new UserCreateRequest("bob", "bob@mail.com", "StrongPass1");
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

}
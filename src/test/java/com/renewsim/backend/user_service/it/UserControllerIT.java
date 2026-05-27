package com.renewsim.backend.user_service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.config.TestSecurityConfig;
import com.renewsim.backend.user_service.web.dto.UserCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private record CreatedUser(long id, String username, String email) {}

    private CreatedUser createUser(String usernamePrefix) throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String username = usernamePrefix + "_" + suffix;
        String email = username + "@mail.com";
        UserCreateRequest request = new UserCreateRequest(username, email, "StrongPass1", null, null);

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        long userId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        return new CreatedUser(userId, username, email);
    }


    // ---------------------------
    // POST /users
    // ---------------------------
    @Test
    @DisplayName("should return 201 Created when user is created successfully")
    @WithMockUser(roles = "ADMIN")
    void testCreateUserReturns201() throws Exception {
        UserCreateRequest request = new UserCreateRequest("bob", "bob@mail.com", "StrongPass1", null, null);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.email").value("bob@mail.com"));
    }

    @Test
    @DisplayName("should return 400 Bad Request when request body is invalid")
    @WithMockUser(roles = "ADMIN")
    void testCreateUserInvalidBodyReturns400() throws Exception {
        UserCreateRequest invalid = new UserCreateRequest("INVALID_UPPER", "not-an-email", "123", null, "bad-phone");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 403 Forbidden when role is insufficient")
    @WithMockUser(roles = "USER")
    void testCreateUserForbiddenReturns403() throws Exception {
        UserCreateRequest request = new UserCreateRequest("charlie", "charlie@mail.com", "StrongPass1", null, null);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return 401 Unauthorized when no token provided")
    void testUnauthorizedReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------
    // GET /users/{id}
    // ---------------------------
    @Test
    @DisplayName("should return 200 OK when user is found")
    @WithMockUser(roles = "ADMIN")
    void testGetUserByIdReturns200() throws Exception {
        CreatedUser user = createUser("lookup");

        mockMvc.perform(get("/api/v1/users/{id}", user.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(user.email()));
    }

    @Test
    @DisplayName("should return 404 Not Found when user does not exist")
    @WithMockUser(roles = "ADMIN")
    void testGetUserByIdNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"));
    }

    // ---------------------------
    // GET /users (list paginada)
    // ---------------------------
    @Test
    @DisplayName("should return paginated list of users")
    @WithMockUser(roles = "ADMIN")
    void testListUsersReturns200() throws Exception {
        createUser("list");
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.page").value(0));
    }

    // ---------------------------
    // GET /users/by-username
    // ---------------------------
    @Test
    @DisplayName("should return 200 OK when user found by username")
    @WithMockUser(roles = "ADMIN")
    void testGetUserByUsernameReturns200() throws Exception {
        CreatedUser user = createUser("byusername");
        mockMvc.perform(get("/api/v1/users/by-username")
                        .param("username", user.username()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(user.email()));
    }

    @Test
    @DisplayName("should return 404 Not Found when username does not exist")
    @WithMockUser(roles = "ADMIN")
    void testGetUserByUsernameNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/users/by-username")
                        .param("username", "ghost"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"));
    }

    // ---------------------------
    // GET /users/by-email
    // ---------------------------
    @Test
    @DisplayName("should return 200 OK when user found by email")
    @WithMockUser(roles = "ADMIN")
    void testGetUserByEmailReturns200() throws Exception {
        CreatedUser user = createUser("byemail");
        mockMvc.perform(get("/api/v1/users/by-email")
                        .param("email", user.email()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(user.email()));
    }

    @Test
    @DisplayName("should return 404 Not Found when email does not exist")
    @WithMockUser(roles = "ADMIN")
    void testGetUserByEmailNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/users/by-email")
                        .param("email", "ghost@mail.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"));
    }

    // ---------------------------
    // GET /users/exists
    // ---------------------------
    @Test
    @DisplayName("should return true if user exists")
    @WithMockUser(roles = "ADMIN")
    void testExistsUserReturnsTrue() throws Exception {
        CreatedUser user = createUser("exists");
        mockMvc.perform(get("/api/v1/users/exists")
                        .param("username", user.username()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("should return false if user does not exist")
    @WithMockUser(roles = "ADMIN")
    void testExistsUserReturnsFalse() throws Exception {
        mockMvc.perform(get("/api/v1/users/exists")
                        .param("username", "ghost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    // ---------------------------
    // GET /users/internal/credentials
    // ---------------------------
    @Test
    @DisplayName("should return credentials when role SERVICE_AUTH")
    @WithMockUser(roles = "SERVICE_AUTH")
    void testGetCredentialsReturns200() throws Exception {
        CreatedUser user = createUser("credentials");
        mockMvc.perform(get("/api/v1/users/internal/credentials")
                        .param("username", user.username()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(user.email()))
                .andExpect(jsonPath("$.data.email").value(user.email()));
    }

    @Test
    @DisplayName("should return 403 Forbidden when role is not SERVICE_AUTH")
    @WithMockUser(roles = "USER")
    void testGetCredentialsForbidden() throws Exception {
        CreatedUser user = createUser("credentialsforbidden");
        mockMvc.perform(get("/api/v1/users/internal/credentials")
                        .param("username", user.username()))
                .andExpect(status().isForbidden());
    }
}



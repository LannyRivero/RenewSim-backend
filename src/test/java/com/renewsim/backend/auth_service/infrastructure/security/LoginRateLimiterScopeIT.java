package com.renewsim.backend.auth_service.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "security.rate-limiting.enabled=true",
    "security.rate-limiting.strategy=IP",
    "security.rate-limiting.max-attempts=1",
    "security.rate-limiting.window=5s",
    "security.rate-limiting.retry-after=5s",
    "security.rate-limiting.login-path=/api/v1/auth/login"
})
class LoginRateLimiterScopeIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;

    private String body() throws Exception {
        var map = new java.util.HashMap<String, Object>();
        map.put("username", "john");
        map.put("password", "x");
        return om.writeValueAsString(map);
    }

    @Test
    @DisplayName("GET /api/v1/auth/login → NO aplica rate limit (solo POST)")
    void getLoginNotLimited() throws Exception {
        mockMvc.perform(get("/api/v1/auth/login"))
               .andExpect(res -> {
                   int s = res.getResponse().getStatus();
                   if (s == 429) throw new AssertionError("Rate-limit shouldn't apply on GET");
               });
    }

    @Test
    @DisplayName("POST /api/v1/auth/register → NO aplica rate limit (scoped solo a login)")
    void registerNotLimited() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(body()))
               .andExpect(res -> {
                   int s = res.getResponse().getStatus();
                   if (s == 429) throw new AssertionError("Rate-limit shouldn't apply on /register");
               });
    }
}


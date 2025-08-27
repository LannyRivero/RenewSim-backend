package com.renewsim.backend.auth_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.application.port.in.AuthUseCase;
import com.renewsim.backend.auth_service.infrastructure.security.AuthNoCacheFilter;
import com.renewsim.backend.auth_service.infrastructure.security.JwtAuthenticationFilter;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;
import com.renewsim.backend.auth_service.web.controller.AuthController;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({ SecurityConfig.class, AuthNoCacheFilter.class })
@TestPropertySource(properties = {
                "cors.allowed-origins=http://localhost:3000",
                "cors.allow-credentials=true",
                "cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS,PATCH",
                "cors.allowed-headers=Content-Type,Authorization,X-Requested-With,X-Correlation-Id",
                "cors.exposed-headers=X-Correlation-Id",
                "security.rate-limiting.enabled=false",
                "server.forward-headers-strategy=framework"
})

@ActiveProfiles("test")
class SecurityConfigTest {

        @Autowired
        private MockMvc mvc;

        @MockBean
        private AuthUseCase authUseCase;
        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;
        @MockBean
        private LoginRateLimitingFilter loginRateLimitingFilter;

        private final ObjectMapper mapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                Mockito.when(authUseCase.login(any())).thenReturn(
                                AuthResponseDTO.builder()
                                                .username("john")
                                                .token("mock-token")
                                                .tokenType("Bearer")
                                                .roles(Set.of("USER"))
                                                .scopes(Set.of("read"))
                                                .expiresAt(Instant.now().plusSeconds(3600))
                                                .build());
        }

        @Test
        @DisplayName("Auth endpoints públicos y login con no-store")
        void authEndpoints_public_y_noStore_en_login() throws Exception {
                String body = """
                                    {"username":"john","password":"secret"}
                                """;

                mvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
                                .andExpect(header().string(HttpHeaders.PRAGMA, containsString("no-cache")))
                                .andExpect(header().exists("Expires"))
                                .andExpect(header().string("X-Correlation-Id", not(emptyOrNullString())));
        }

        @Test
        @DisplayName("Security headers should be present (CSP, HSTS, XFO, Referrer-Policy)")
        void security_headers_present_on_public_endpoint() throws Exception {
                mvc.perform(get("/error").secure(true))
                                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                                .andExpect(header().string("X-Frame-Options", "DENY"))
                                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                                .andExpect(header().string("Content-Security-Policy", containsString("default-src")))
                                .andExpect(header().string("Strict-Transport-Security", containsString("max-age")));
        }

        @Test
        @DisplayName("HSTS se envía cuando X-Forwarded-Proto=https (detrás de proxy TLS)")
        void hsts_with_forwarded_proto_https() throws Exception {
                mvc.perform(get("/error").secure(true)
                                .header("X-Forwarded-Proto", "https"))
                                .andExpect(header().string("Strict-Transport-Security", containsString("max-age")));
        }
        
}

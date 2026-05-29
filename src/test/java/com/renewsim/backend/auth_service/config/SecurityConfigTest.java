package com.renewsim.backend.auth_service.config;

import com.renewsim.backend.auth_service.application.port.in.LoginUseCase;
import com.renewsim.backend.auth_service.application.port.in.LogoutUseCase;
import com.renewsim.backend.auth_service.application.port.in.RefreshTokenUseCase;
import com.renewsim.backend.auth_service.application.port.in.RegisterUserUseCase;
import com.renewsim.backend.auth_service.infrastructure.config.SecurityConfig;
import com.renewsim.backend.auth_service.infrastructure.security.AuthNoCacheFilter;
import com.renewsim.backend.auth_service.infrastructure.security.JwtAuthenticationFilter;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;
import com.renewsim.backend.auth_service.web.controller.AuthController;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({ SecurityConfig.class, AuthNoCacheFilter.class, TestSecurityConfig.class })
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

        @Autowired
        private RoleHierarchy roleHierarchy;

        @MockitoBean
        private LogoutUseCase logoutUseCase;
        @MockitoBean
        private RefreshTokenUseCase refreshTokenUseCase;
        @MockitoBean
        private RegisterUserUseCase registerUserUseCase;
        @MockitoBean
        private LoginUseCase loginUseCase;
        @MockitoBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;
        @MockitoBean
        private LoginRateLimitingFilter loginRateLimitingFilter;

        @Test
        @DisplayName("Public auth endpoints and login with no-store")
        void authEndpoints_public_and_noStore_on_login() throws Exception {
                String body = """
                                {"email":"john@test.com","password":"secret"}
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
        @DisplayName("HSTS is sent when X-Forwarded-Proto=https (behind TLS proxy)")
        void hsts_with_forwarded_proto_https() throws Exception {
                mvc.perform(get("/error").secure(true)
                                .header("X-Forwarded-Proto", "https"))
                                .andExpect(header().string("Strict-Transport-Security", containsString("max-age")));
        }

        @Test
        @DisplayName("Role hierarchy should place ANALYST between ADMIN and USER")
        void roleHierarchy_supportsAnalyst() {
                var reachable = roleHierarchy.getReachableGrantedAuthorities(
                                List.of(new SimpleGrantedAuthority("ROLE_ANALYST")));

                org.assertj.core.api.Assertions.assertThat(reachable)
                                .extracting(a -> a.getAuthority())
                                .contains("ROLE_ANALYST", "ROLE_USER")
                                .doesNotContain("ROLE_ADMIN");
        }
}

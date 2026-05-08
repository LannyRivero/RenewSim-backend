package com.renewsim.backend.auth_service.config;

import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.infrastructure.config.SecurityConfig;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;
import com.renewsim.backend.auth_service.support.TestSecuredController;
import com.renewsim.backend.config.TestSecurityConfig;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {
                TestSecuredController.class,
                SecurityConfig.class,
                SecurityTestBeans.class,
                MethodSecurityTestConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class SecurityIntegrationTest {

        @Resource
        private MockMvc mockMvc;

        @MockBean
        private TokenProvider tokenProvider;

        @MockBean
        private LoginRateLimitingFilter loginRateLimitingFilter;

        private static String bearer(String token) {
                return "Bearer " + token;
        }

        @BeforeEach
        void allowFilterChain() throws Exception {
                doAnswer(inv -> {
                        ServletRequest req = inv.getArgument(0);
                        ServletResponse res = inv.getArgument(1);
                        FilterChain chain = inv.getArgument(2);
                        chain.doFilter(req, res);
                        return null;
                }).when(loginRateLimitingFilter).doFilter(any(), any(), any());
        }

        @Test
        @DisplayName("GIVEN roles=[ADMIN], WHEN authenticated, THEN /admin -> 200 (ROLE_ADMIN)")
        void adminRoleAllowsAccess() throws Exception {
                Mockito.when(tokenProvider.validate(anyString()))
                                .thenReturn(Optional.of(new AuthenticatedUser("john", Set.of("ADMIN"), Set.of())));

                mockMvc.perform(get("/test-secure/admin")
                                .header(HttpHeaders.AUTHORIZATION, bearer("fake-token")))
                                .andExpect(status().isOk())
                                .andExpect(content().string("ok-admin"));
        }

        @Test
        @DisplayName("GIVEN scopes=[read:simulations], WHEN authenticated, THEN /read-simulations -> 200 (SCOPE_read:simulations)")
        void scopeAllowsAccess() throws Exception {
                Mockito.when(tokenProvider.validate(anyString()))
                                .thenReturn(Optional.of(
                                                new AuthenticatedUser("john", Set.of(), Set.of("read:simulations"))));

                mockMvc.perform(get("/test-secure/read-simulations")
                                .header(HttpHeaders.AUTHORIZATION, bearer("fake-token")))
                                .andExpect(status().isOk())
                                .andExpect(content().string("ok-scope"));
        }

        @Test
        @DisplayName("WITHOUT required authorities -> 403 (denied by @PreAuthorize)")
        void forbiddenWithoutAuthorities() throws Exception {
                Mockito.when(tokenProvider.validate(anyString()))
                                .thenReturn(Optional.of(new AuthenticatedUser("john", Set.of(), Set.of())));

                mockMvc.perform(get("/test-secure/admin")
                                .header(HttpHeaders.AUTHORIZATION, bearer("fake-token")))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Security headers present and not duplicated on 200")
        void securityHeadersOn200() throws Exception {
                Mockito.when(tokenProvider.validate(anyString()))
                                .thenReturn(Optional.of(new AuthenticatedUser("john", Set.of("ADMIN"), Set.of())));

                mockMvc.perform(get("/test-secure/admin")
                                .header(HttpHeaders.AUTHORIZATION, bearer("fake-token")))
                                .andExpect(status().isOk())
                                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                                .andExpect(header().string("X-Frame-Options", "DENY"))
                                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                                .andExpect(header().exists("Content-Security-Policy"));
        }

}

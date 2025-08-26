package com.renewsim.backend.auth_service.config;

import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.infrastructure.security.JwtAuthenticationFilter;
import com.renewsim.backend.auth_service.support.TestSecuredController;

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

import jakarta.annotation.Resource;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {
    TestSecuredController.class,
    com.renewsim.backend.auth_service.infrastructure.security.JwtAuthenticationFilter.class,
    com.renewsim.backend.auth_service.config.SecurityConfig.class,
    TestMvcBeans.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({JwtAuthenticationFilter.class}) 
class SecurityIntegrationTest {

    @Resource
    private MockMvc mockMvc;

    @MockBean
    private TokenProvider tokenProvider; 

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    @DisplayName("DADO roles=[ADMIN], CUANDO autentica, ENTONCES /admin → 200 (ROLE_ADMIN)")
    void adminRoleAllowsAccess() throws Exception {
        Mockito.when(tokenProvider.validate(anyString()))
                .thenReturn(Optional.of(new AuthenticatedUser(
                        "john", Set.of("ADMIN"), Set.of()
                )));

        mockMvc.perform(get("/test-secure/admin")
                        .header(HttpHeaders.AUTHORIZATION, bearer("fake-token")))
                .andExpect(status().isOk())
                .andExpect(content().string("ok-admin"));
    }

    @Test
    @DisplayName("DADO scopes=[read:simulations], CUANDO autentica, ENTONCES /read-simulations → 200 (SCOPE_read:simulations)")
    void scopeAllowsAccess() throws Exception {
        Mockito.when(tokenProvider.validate(anyString()))
                .thenReturn(Optional.of(new AuthenticatedUser(
                        "john", Set.of(), Set.of("read:simulations")
                )));

        mockMvc.perform(get("/test-secure/read-simulations")
                        .header(HttpHeaders.AUTHORIZATION, bearer("fake-token")))
                .andExpect(status().isOk())
                .andExpect(content().string("ok-scope"));
    }

    @Test
    @DisplayName("SIN authorities adecuadas → 403 (denegado por @PreAuthorize)")
    void forbiddenWithoutAuthorities() throws Exception {
        Mockito.when(tokenProvider.validate(anyString()))
                .thenReturn(Optional.of(new AuthenticatedUser(
                        "john", Set.of(), Set.of()
                )));

        mockMvc.perform(get("/test-secure/admin")
                        .header(HttpHeaders.AUTHORIZATION, bearer("fake-token")))
                .andExpect(status().isForbidden());
    }
}

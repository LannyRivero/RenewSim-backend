package com.renewsim.backend.auth_service.infrastructure.security;

import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.config.MethodSecurityTestConfig;
import com.renewsim.backend.auth_service.config.SecurityTestBeans;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.infrastructure.config.SecurityConfig;
import com.renewsim.backend.auth_service.support.TestSecuredController;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(classes = {
        TestSecuredController.class,
        SecurityConfig.class,
        SecurityTestBeans.class,
        MethodSecurityTestConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthNoCacheFilterIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TokenProvider tokenProvider;

    @MockBean
    LoginRateLimitingFilter loginRateLimitingFilter;

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
    @DisplayName("Auth endpoints → añade no-cache (por AuthNoCacheFilter)")
    void authEndpointsHaveNoStore() throws Exception {
        var res = mockMvc.perform(post("/api/v1/auth/login"))
                .andReturn()
                .getResponse();

        assertThat(res.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(res.getHeader("Pragma")).isEqualTo("no-cache");
        assertThat(res.getHeader("Expires")).isNotBlank();

        assertSingleHeader(res, "Cache-Control");
        assertSingleHeader(res, "Pragma");
        assertSingleHeader(res, "Expires");
    }

    @Test
    @DisplayName("Endpoints no-auth (200 OK) → NO añade no-cache (no lo pone el filtro)")
    void nonAuth200DoesNotAddNoStore() throws Exception {
        Mockito.when(tokenProvider.validate(anyString()))
                .thenReturn(Optional.of(new AuthenticatedUser("john", Set.of("ADMIN"), Set.of())));

        var res = mockMvc.perform(get("/test-secure/admin")
                .header(HttpHeaders.AUTHORIZATION, bearer("fake-token")))
                .andReturn()
                .getResponse();

        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(res.getHeader("Cache-Control")).isNull();
        assertThat(res.getHeader("Pragma")).isNull();
        assertThat(res.getHeader("Expires")).isNull();
    }

    @Test
    @DisplayName("401 de endpoint protegido → no-cache viene del exceptionHandling global (valor distinto)")
    void protected401HasNoStoreFromExceptionHandler() throws Exception {
        var res = mockMvc.perform(get("/test-secure/admin"))
                .andReturn()
                .getResponse();

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getHeader("Cache-Control")).isEqualTo("no-store, max-age=0");
        assertThat(res.getHeader("Pragma")).isEqualTo("no-cache");
        assertThat(res.getHeader("Expires")).isEqualTo("0");
    }

    private static void assertSingleHeader(org.springframework.mock.web.MockHttpServletResponse res, String name) {
        var values = res.getHeaders(name);
        assertThat(values)
                .withFailMessage("Header %s should appear exactly once, got: %s", name, values)
                .hasSize(1);
    }
}

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
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(classes = {
        TestSecuredController.class,
        SecurityConfig.class,
        SecurityTestBeans.class,
        MethodSecurityTestConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityHeadersIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TokenProvider tokenProvider;

    @MockBean
    LoginRateLimitingFilter loginRateLimitingFilter;

    private static final String OK_ENDPOINT = "/test-secure/admin";
    private static final String PROTECTED_ENDPOINT = "/api/v1/me";
    private static final String ADMIN_ENDPOINT = "/api/v1/admin";

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
    @DisplayName("200 OK -> Security headers present")
    void headersOn200() throws Exception {
        Mockito.when(tokenProvider.validate(anyString()))
                .thenReturn(Optional.of(new AuthenticatedUser("john", Set.of("ADMIN"), Set.of())));

        MvcResult res = mockMvc.perform(get(OK_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, bearer("fake-token")))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertSecurityHeaders(res);
    }

    @Test
    @DisplayName("Protected -> Security headers present on 401/403")
    void headersOnProtected() throws Exception {
        MvcResult res = mockMvc.perform(get(PROTECTED_ENDPOINT)).andReturn();
        int status = res.getResponse().getStatus();
        assertThat(Set.of(401, 403)).as("expected 401 or 403").contains(status);
        assertSecurityHeaders(res);
    }

    @Test
    @DisplayName("Admin -> Security headers present on 401/403")
    void headersOnAdmin() throws Exception {
        MvcResult res = mockMvc.perform(get(ADMIN_ENDPOINT)).andReturn();
        int status = res.getResponse().getStatus();
        assertThat(Set.of(401, 403)).as("expected 401 or 403").contains(status);
        assertSecurityHeaders(res);
    }

    @Test
    @DisplayName("Simulated HTTPS -> HSTS present only once")
    void hstsWhenHttps() throws Exception {
        MvcResult res = mockMvc.perform(get(PROTECTED_ENDPOINT)
                .header("X-Forwarded-Proto", "https"))
                .andReturn();

        var hsts = res.getResponse().getHeaders("Strict-Transport-Security");
        assertThat(hsts.size()).isLessThanOrEqualTo(1);
        if (!hsts.isEmpty()) {
            assertThat(hsts.getFirst()).contains("max-age=").contains("includeSubDomains");
        }
    }

    private void assertSecurityHeaders(MvcResult res) {
        var response = res.getResponse();

        assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeader("X-Frame-Options")).isEqualTo("DENY");
        assertThat(response.getHeader("Referrer-Policy")).isEqualTo("no-referrer");

        String csp = response.getHeader("Content-Security-Policy");
        assertThat(csp).isNotBlank();
        assertCspContains(csp, "default-src 'none'");
        assertCspContains(csp, "connect-src 'self'");
        assertCspContains(csp, "frame-ancestors 'none'");
        assertCspContains(csp, "base-uri 'none'");
        assertCspContains(csp, "form-action 'none'");
        assertCspContains(csp, "object-src 'none'");
        assertCspContains(csp, "block-all-mixed-content");

        String pp = response.getHeader("Permissions-Policy");
        assertThat(pp).isNotBlank();
        assertThat(pp).contains("geolocation=()", "microphone=()", "camera=()");

        assertSingleHeader(res, "X-Content-Type-Options");
        assertSingleHeader(res, "X-Frame-Options");
        assertSingleHeader(res, "Referrer-Policy");
        assertSingleHeader(res, "Content-Security-Policy");
        assertSingleHeader(res, "Permissions-Policy");

        List<String> hsts = response.getHeaders("Strict-Transport-Security");
        assertThat(hsts.size()).isLessThanOrEqualTo(1);
        if (!hsts.isEmpty()) {
            assertThat(hsts.getFirst()).contains("max-age=").contains("includeSubDomains");
        }
    }

    private void assertSingleHeader(MvcResult res, String name) {
        List<String> values = res.getResponse().getHeaders(name);
        assertThat(values)
                .withFailMessage("Header %s should appear exactly once, got: %s", name, values)
                .hasSize(1);
    }

    private void assertCspContains(String csp, String directive) {
        assertThat(csp)
                .withFailMessage("CSP should contain directive: %s%nCSP was: %s", directive, csp)
                .contains(directive);
    }
}

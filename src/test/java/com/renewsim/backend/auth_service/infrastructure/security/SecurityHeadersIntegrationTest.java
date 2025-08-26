package com.renewsim.backend.auth_service.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityHeadersIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    private static final String PROTECTED_ENDPOINT = "/api/v1/me";
    private static final String ADMIN_ENDPOINT = "/api/v1/admin";

    @Test
    @DisplayName("Protected → Security headers present on 401/403")
    void headersOnProtected() throws Exception {
        MvcResult res = mockMvc.perform(get(PROTECTED_ENDPOINT))
                .andReturn();

        int status = res.getResponse().getStatus();
        assertThat(Set.of(401, 403)).as("expected 401 or 403").contains(status);
        assertSecurityHeaders(res);
    }

    @Test
    @DisplayName("Admin → Security headers present on 401/403")
    void headersOnAdmin() throws Exception {
        MvcResult res = mockMvc.perform(get(ADMIN_ENDPOINT))
                .andReturn();

        int status = res.getResponse().getStatus();
        assertThat(Set.of(401, 403)).as("expected 401 or 403").contains(status);
        assertSecurityHeaders(res);
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
                .withFailMessage("CSP should contain directive: %s\nCSP was: %s", directive, csp)
                .contains(directive);
    }
}

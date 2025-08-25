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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityHeadersIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    private static final String OK_200_ENDPOINT = "/actuator/health";
    private static final String UNAUTHORIZED_401_ENDPOINT = "/api/v1/me";  
    private static final String FORBIDDEN_403_ENDPOINT = "/api/v1/admin";   
    private static final String CONFLICT_409_ENDPOINT = "/api/v1/auth/register";


    @Test
    @DisplayName("200 → Security headers present and not duplicated")
    void headersOn200() throws Exception {
        MvcResult res = mockMvc.perform(get(OK_200_ENDPOINT))
                .andExpect(status().isOk())
                .andReturn();
        assertSecurityHeaders(res);
    }

    @Test
    @DisplayName("401 → Security headers present and not duplicated")
    void headersOn401() throws Exception {
        MvcResult res = mockMvc.perform(get(UNAUTHORIZED_401_ENDPOINT))
                .andExpect(status().isUnauthorized())
                .andReturn();
        assertSecurityHeaders(res);
    }

    @Test
    @DisplayName("403 → Security headers present and not duplicated")
    void headersOn403() throws Exception {
        MvcResult res = mockMvc.perform(get(FORBIDDEN_403_ENDPOINT))
                .andExpect(status().isForbidden())
                .andReturn();
        assertSecurityHeaders(res);
    }

    @Test
    @DisplayName("409 → Security headers present and not duplicated")
    void headersOn409() throws Exception {
        String payload = """
            {"username":"existing@example.com","password":"Secret123!","fullName":"Existing"}
            """;
        MvcResult res = mockMvc.perform(
                        post(CONFLICT_409_ENDPOINT)
                                .contentType("application/json")
                                .content(payload))
                .andExpect(status().isConflict())
                .andReturn();
        assertSecurityHeaders(res);
    }


/*************  ✨ Windsurf Command ⭐  *************/
    /**
     * Asserts that the security headers are present and not duplicated.
     *
     * <ul>
     *     <li>X-Content-Type-Options: nosniff</li>
     *     <li>X-Frame-Options: DENY</li>
     *     <li>Referrer-Policy: no-referrer</li>
     *     <li>Content-Security-Policy: non-empty</li>
     * </ul>
     *
     * <p>Also asserts that there is only one header for each of the above.</p>
     *
     * <p>If the request is secure (HTTPS), also asserts that the Strict-Transport-Security header is present and
     * has the correct format (max-age=...).</p>
     *
     * @param res the MvcResult to check
     */
/*******  51c26b1a-8ddd-44f0-9cbe-281a24b5651d  *******/    private void assertSecurityHeaders(MvcResult res) {
        var response = res.getResponse();

        // Presentes
        assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeader("X-Frame-Options")).isEqualTo("DENY");
        assertThat(response.getHeader("Referrer-Policy")).isEqualTo("no-referrer");
        assertThat(response.getHeader("Content-Security-Policy")).isNotBlank();

        // No duplicados (size == 1)
        assertSingleHeader(res, "X-Content-Type-Options");
        assertSingleHeader(res, "X-Frame-Options");
        assertSingleHeader(res, "Referrer-Policy");
        assertSingleHeader(res, "Content-Security-Policy");

        // HSTS solo si el request es seguro; en MockMvc (HTTP) normalmente será null o vacío.
        List<String> hsts = res.getResponse().getHeaders("Strict-Transport-Security");
        assertThat(hsts.size()).isLessThanOrEqualTo(1);
        // Si está presente, valida formato
        if (!hsts.isEmpty()) {
            assertThat(hsts.get(0)).startsWith("max-age=");
        }
    }

    private void assertSingleHeader(MvcResult res, String name) {
        List<String> values = res.getResponse().getHeaders(name);
        assertThat(values).hasSize(1);
    }
}


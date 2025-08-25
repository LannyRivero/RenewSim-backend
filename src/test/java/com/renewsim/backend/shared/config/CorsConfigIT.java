package com.renewsim.backend.shared.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.anyOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsConfigIT {

    @Autowired
    private MockMvc mockMvc;

    private static final String ALLOWED_ORIGIN = "https://app.renewsim.com";
    private static final String DISALLOWED_ORIGIN = "https://evil.example.com";

    @Test
    @DisplayName("OPTIONS con origen permitido → devuelve cabeceras CORS")
    void preflight_allowedOrigin_returnsCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                // Algunos setups devuelven 200 o 204. Aceptamos cualquiera.
                .andExpect(status().is(anyOf(is(200), is(204))))
                .andExpect(header().string("Vary", containsString("Origin")))
                .andExpect(header().string("Access-Control-Allow-Origin", is(ALLOWED_ORIGIN)))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("Authorization")))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("Content-Type")))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Origen no permitido → 403 o sin cabeceras CORS")
    void preflight_disallowedOrigin_forbiddenOrNoCorsHeaders() throws Exception {
        var result = mockMvc.perform(options("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, DISALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andReturn();

        int status = result.getResponse().getStatus();
        String allowOrigin = result.getResponse().getHeader("Access-Control-Allow-Origin");

        // Dos comportamientos válidos:
        // 1) 403 Forbidden
        // 2) 200/204 pero SIN cabeceras CORS (no expone recursos cross-origin)
        if (status == 403) {
            // OK: denegado por CORS
        } else {
            // Si no hay 403, debe NO incluir cabeceras CORS
            org.assertj.core.api.Assertions.assertThat(allowOrigin)
                    .as("Access-Control-Allow-Origin debe estar ausente para orígenes no permitidos")
                    .isNull();
        }
    }
}


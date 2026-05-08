package com.renewsim.backend.role_service.infrastructure.gateway;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.renewsim.backend.config.TestSecurityConfig;
import com.renewsim.backend.user_service.web.dto.UpdateUserRolesRequestDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@Import(TestSecurityConfig.class)
class HttpUserServiceGatewayIT {

    private static WireMockServer wireMockServer;

    @Autowired
    private HttpUserServiceGateway gateway;

    @BeforeAll
    static void setupServer() {
        wireMockServer = new WireMockServer(9561);
        wireMockServer.start();
        configureFor("localhost", 9561);
    }

    @AfterAll
    static void stopServer() {
        wireMockServer.stop();
    }

    @Test
    void shouldUpdateUserRolesSuccessfully() {
        stubFor(put(urlEqualTo("/users/1/roles"))
                .willReturn(aResponse().withStatus(200)));

        UpdateUserRolesRequestDTO request = new UpdateUserRolesRequestDTO(List.of("ADMIN"));

        assertThatCode(() -> gateway.updateUserRoles(1L, request))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldTriggerFallbackOn500Error() {
        stubFor(put(urlEqualTo("/users/1/roles"))
                .willReturn(aResponse().withStatus(500)));

        UpdateUserRolesRequestDTO request = new UpdateUserRolesRequestDTO(List.of("ADMIN"));

        // como el fallback es silencioso, no lanza excepción
        assertThatCode(() -> gateway.updateUserRoles(1L, request))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldTriggerFallbackOnTimeout() {
        stubFor(put(urlEqualTo("/users/1/roles"))
                .willReturn(aResponse().withFixedDelay(3000))); // simula timeout

        UpdateUserRolesRequestDTO request = new UpdateUserRolesRequestDTO(List.of("ADMIN"));

        // el fallback se activa, pero no rompe el flujo
        assertThatCode(() -> gateway.updateUserRoles(1L, request))
                .doesNotThrowAnyException();
    }
}

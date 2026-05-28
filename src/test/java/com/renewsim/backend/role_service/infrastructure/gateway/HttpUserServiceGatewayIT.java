package com.renewsim.backend.role_service.infrastructure.gateway;


import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import com.renewsim.backend.role_service.application.dto.UserRolesUpdateRequest;
import com.renewsim.backend.role_service.application.port.out.UserServiceGateway;
import com.renewsim.backend.config.TestSecurityConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "user-service.url=http://localhost:9561")
@Import(TestSecurityConfig.class)
class HttpUserServiceGatewayIT {

    private static WireMockServer wireMockServer;

    @Autowired
    private UserServiceGateway gateway;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

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

    @BeforeEach
    void resetState() {
        wireMockServer.resetAll();
        circuitBreakerRegistry.circuitBreaker("userService").reset();
    }

    @Test
    void shouldUpdateUserRolesSuccessfully() {
        stubFor(put(urlEqualTo("/api/v1/users/1/roles"))
                .willReturn(aResponse().withStatus(200)));

        UserRolesUpdateRequest request = new UserRolesUpdateRequest(List.of("ADMIN"));

        assertThatCode(() -> gateway.updateUserRoles(1L, request))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldTriggerFallbackOn500Error() {
        stubFor(put(urlEqualTo("/api/v1/users/1/roles"))
                .willReturn(aResponse().withStatus(500)));

        UserRolesUpdateRequest request = new UserRolesUpdateRequest(List.of("ADMIN"));

        assertThatThrownBy(() -> gateway.updateUserRoles(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unable to update user roles");
    }

    @Test
    void shouldTriggerFallbackOnTimeout() {
        stubFor(put(urlEqualTo("/api/v1/users/1/roles"))
                .willReturn(aResponse().withFixedDelay(3000))); // simula timeout

        UserRolesUpdateRequest request = new UserRolesUpdateRequest(List.of("ADMIN"));

        assertThatCode(() -> gateway.updateUserRoles(1L, request))
                .doesNotThrowAnyException();
    }
}

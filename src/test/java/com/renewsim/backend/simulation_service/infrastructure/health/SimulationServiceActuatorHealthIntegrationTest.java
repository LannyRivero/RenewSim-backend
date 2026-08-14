package com.renewsim.backend.simulation_service.infrastructure.health;

import com.renewsim.backend.auth_service.infrastructure.config.ActuatorSecurityConfig;
import com.renewsim.backend.auth_service.infrastructure.security.SecurityHeadersFilter;
import com.renewsim.backend.shared.observability.CorrelationIdFilter;
import com.renewsim.backend.simulation_service.infrastructure.config.PvgisProperties;
import com.renewsim.backend.simulation_service.infrastructure.config.SimulationClimateProperties;
import com.renewsim.backend.simulation_service.infrastructure.config.WeatherServiceProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SimulationServiceActuatorHealthIntegrationTest.TestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestPropertySource(properties = {
        "management.endpoints.web.exposure.include=health",
        "management.endpoint.health.show-details=always"
})
class SimulationServiceActuatorHealthIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("local actuator exposes simulationService health contributor details")
    void actuatorHealthExposesSimulationServiceContributor() throws Exception {
        mvc.perform(get("/actuator/health/simulationService"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.details.climateProvider").value("dummy"))
                .andExpect(jsonPath("$.details.pvgisConfigured").value(true));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class
    })
    @Import({ ActuatorSecurityConfig.class, SecurityHeadersFilter.class, CorrelationIdFilter.class,
            SimulationServiceHealthIndicator.class })
    static class TestApp {

        @Bean
        SimulationClimateProperties simulationClimateProperties() {
            return new SimulationClimateProperties("dummy");
        }

        @Bean
        WeatherServiceProperties weatherServiceProperties() {
            return new WeatherServiceProperties("https://api.openweathermap.org", "dummy-key",
                    Duration.ofSeconds(2), Duration.ofSeconds(3));
        }

        @Bean
        PvgisProperties pvgisProperties() {
            return new PvgisProperties("https://re.jrc.ec.europa.eu", Duration.ofSeconds(2), Duration.ofSeconds(5));
        }
    }
}

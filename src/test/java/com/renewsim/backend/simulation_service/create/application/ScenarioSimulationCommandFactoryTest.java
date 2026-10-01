package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.command.CreateSimulationFromScenarioCommand;
import com.renewsim.backend.simulation_service.domain.exception.InvalidConsumptionProfileException;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationCurrencyException;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.shared.application.port.out.ScenarioLookupPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScenarioSimulationCommandFactoryTest {

        private final ScenarioSimulationCommandFactory factory = new ScenarioSimulationCommandFactory(
                        new ScenarioSimulationDefaultsPolicy());

        @Test
        @DisplayName("fromScenario maps scenario defaults into a real simulation command")
        void fromScenarioMapsScenarioDefaultsIntoRealSimulationCommand() {
                CreateRealSimulationCommand command = factory.fromScenario(
                                request(null),
                                scenario("EUR", 6000.0),
                                "solar",
                                List.of(1L, 2L));

                assertThat(command.name()).isEqualTo("Hogar solar - Sevilla");
                assertThat(command.technology().value()).isEqualTo("solar");
                assertThat(command.location().label()).isEqualTo("Sevilla, Andalucia, ES");
                assertThat(command.system().installedCapacityKw()).isEqualTo(5.0);
                assertThat(command.demand().annualConsumptionKwh()).isEqualTo(6000.0);
                assertThat(command.economics().capexTotal()).isEqualTo(12000.0);
                assertThat(command.economics().currency().value()).isEqualTo("EUR");
                assertThat(command.technologyIds()).containsExactly(1L, 2L);
                assertThat(command.scenarioId()).isEqualTo(7L);
                assertThat(command.createdBy()).isEqualTo("alice");
        }

        @Test
        @DisplayName("fromScenario keeps request name when provided")
        void fromScenarioKeepsRequestNameWhenProvided() {
                CreateRealSimulationCommand command = factory.fromScenario(
                                request("Mi simulacion personalizada"),
                                scenario("EUR", 6000.0),
                                "solar",
                                List.of(1L, 2L));

                assertThat(command.name()).isEqualTo("Mi simulacion personalizada");
        }

        @Test
        @DisplayName("fromScenario falls back to scenario name when request name is blank")
        void fromScenarioFallsBackToScenarioNameWhenRequestNameIsBlank() {
                CreateRealSimulationCommand command = factory.fromScenario(
                                request("   "),
                                scenario("EUR", 6000.0),
                                "solar",
                                List.of(1L, 2L));

                assertThat(command.name()).isEqualTo("Hogar solar - Sevilla");
        }

        @Test
        @DisplayName("fromScenario fails when scenario consumption is not positive")
        void fromScenarioFailsWhenScenarioConsumptionIsNotPositive() {
                assertThatThrownBy(() -> factory.fromScenario(
                                request(null),
                                scenario("EUR", 0.0),
                                "solar",
                                List.of(1L, 2L)))
                                .isInstanceOf(InvalidConsumptionProfileException.class)
                                .hasMessage("VALIDATION_ERROR: scenario defaultConsumption must be positive");
        }

        @Test
        @DisplayName("fromScenario rejects unsupported scenario currency")
        void fromScenarioRejectsUnsupportedScenarioCurrency() {
                assertThatThrownBy(() -> factory.fromScenario(
                                request(null),
                                scenario("USD", 6000.0),
                                "solar",
                                List.of(1L, 2L)))
                                .isInstanceOf(InvalidSimulationCurrencyException.class)
                                .hasMessage("VALIDATION_ERROR: scenario defaultInvestmentCurrency must be EUR");
        }

        @Test
        @DisplayName("fromScenario accepts supported scenario currency with surrounding whitespace")
        void fromScenarioAcceptsSupportedScenarioCurrencyWithWhitespace() {
                CreateRealSimulationCommand command = factory.fromScenario(
                                request(null),
                                scenario(" EUR ", 6000.0),
                                "solar",
                                List.of(1L, 2L));

                assertThat(command.economics().currency().value()).isEqualTo("EUR");
        }

        private CreateSimulationFromScenarioCommand request(String name) {
                return new CreateSimulationFromScenarioCommand(
                                7L,
                                name,
                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain",
                                                CountryCode.of("ES")),
                                "alice");
        }

        private ScenarioLookupPort.ScenarioSnapshot scenario(String currency, double consumption) {
                return new ScenarioLookupPort.ScenarioSnapshot(
                                7L, "Hogar solar - Sevilla", 1L,
                                5.0, 12000.0, currency, 0.15, consumption);
        }
}

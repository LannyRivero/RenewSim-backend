package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.shared.exception.BadRequestException;
import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.command.CreateSimulationFromScenarioCommand;
import com.renewsim.backend.simulation_service.create.application.port.in.CreateRealSimulationUseCase;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationTechnologyException;
import com.renewsim.backend.simulation_service.domain.exception.SimulationScenarioNotFoundException;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.port.out.ScenarioLookupPort;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies scenario lookup, validation, and delegation to the real-simulation
 * use case.
 * Command mapping is covered by {@link ScenarioSimulationCommandFactoryTest};
 * persistence snapshots are covered by
 * {@link CreateSimulationFromScenarioSnapshotTest}.
 */
@ExtendWith(MockitoExtension.class)
class CreateSimulationFromScenarioServiceTest {

        @Mock
        private ScenarioLookupPort scenarioLookupPort;
        @Mock
        private TechnologyLookupPort technologyLookupPort;
        @Mock
        private CreateRealSimulationUseCase createRealSimulationUseCase;

        private final ScenarioSimulationCommandFactory commandFactory = new ScenarioSimulationCommandFactory(
                        new ScenarioSimulationDefaultsPolicy());
        private CreateSimulationFromScenarioService service;

        @BeforeEach
        void setUp() {
                service = new CreateSimulationFromScenarioService(
                                scenarioLookupPort,
                                technologyLookupPort,
                                createRealSimulationUseCase,
                                commandFactory);
        }

        @Test
        @DisplayName("resolves scenario data and delegates a real simulation command")
        void resolvesScenarioDataAndDelegatesRealSimulationCommand() {
                when(scenarioLookupPort.findActiveScenarioById(7L)).thenReturn(Optional.of(scenarioSnapshot()));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(1L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.recommendActiveTechnologyIdsByEnergyType("solar"))
                                .thenReturn(List.of(1L, 2L));
                var expectedResult = new SimulationDetailsResult("60", null, null, null, null, null,
                                null, null, null, null, null, null, List.of());
                when(createRealSimulationUseCase.createSimulation(any())).thenReturn(expectedResult);

                SimulationDetailsResult result = service.createSimulationFromScenario(command());

                assertThat(result).isSameAs(expectedResult);
                ArgumentCaptor<CreateRealSimulationCommand> captor = ArgumentCaptor
                                .forClass(CreateRealSimulationCommand.class);
                verify(createRealSimulationUseCase).createSimulation(captor.capture());
                CreateRealSimulationCommand delegatedCommand = captor.getValue();
                assertThat(delegatedCommand.scenarioId()).isEqualTo(7L);
                assertThat(delegatedCommand.name()).isEqualTo("Hogar solar - Sevilla");
                assertThat(delegatedCommand.technology().value()).isEqualTo("solar");
                assertThat(delegatedCommand.technologyIds()).containsExactly(1L, 2L);
                assertThat(delegatedCommand.system().installedCapacityKw()).isEqualTo(5.0);
                assertThat(delegatedCommand.demand().annualConsumptionKwh()).isEqualTo(6000.0);
                assertThat(delegatedCommand.economics().capexTotal()).isEqualTo(12000.0);
                assertThat(delegatedCommand.createdBy()).isEqualTo("alice");
        }

        @Nested
        @DisplayName("when scenario validation fails")
        class ScenarioValidation {

                @Test
                @DisplayName("rejects a missing scenario")
                void rejectsMissingScenario() {
                        when(scenarioLookupPort.findActiveScenarioById(99L)).thenReturn(Optional.empty());

                        assertThatThrownBy(() -> service.createSimulationFromScenario(command(99L)))
                                        .isInstanceOf(SimulationScenarioNotFoundException.class);

                        verify(technologyLookupPort, never()).findActiveEnergyTypeByTechnologyId(any());
                        verify(createRealSimulationUseCase, never()).createSimulation(any());
                }

                @Test
                @DisplayName("rejects a scenario with an inactive technology")
                void rejectsInactiveTechnology() {
                        when(scenarioLookupPort.findActiveScenarioById(7L)).thenReturn(Optional.of(scenarioSnapshot()));
                        when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(1L)).thenReturn(Optional.empty());

                        assertThatThrownBy(() -> service.createSimulationFromScenario(command()))
                                        .isInstanceOf(InvalidSimulationTechnologyException.class)
                                        .isInstanceOf(BadRequestException.class)
                                        .hasMessage("UNSUPPORTED_SCENARIO_TECHNOLOGY: technologyId '1' is not registered or is inactive in the technology catalog");

                        verify(createRealSimulationUseCase, never()).createSimulation(any());
                }
        }

        private CreateSimulationFromScenarioCommand command() {
                return command(7L);
        }

        private CreateSimulationFromScenarioCommand command(Long scenarioId) {
                return new CreateSimulationFromScenarioCommand(
                                scenarioId,
                                null,
                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845,
                                                "Spain", CountryCode.of("ES")),
                                "alice");
        }

        private ScenarioLookupPort.ScenarioSnapshot scenarioSnapshot() {
                return new ScenarioLookupPort.ScenarioSnapshot(
                                7L, "Hogar solar - Sevilla", 1L,
                                5.0, 12000.0, "EUR", 0.15, 6000.0);
        }
}

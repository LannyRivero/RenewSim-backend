package com.renewsim.backend.simulation_service.create.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.create.application.command.CreateSimulationFromScenarioCommand;
import com.renewsim.backend.simulation_service.create.application.port.in.CreateRealSimulationUseCase;
import com.renewsim.backend.simulation_service.create.application.port.out.PvgisSolarResourcePort;
import com.renewsim.backend.simulation_service.create.application.technology.hydro.HydroSimulationEngine;
import com.renewsim.backend.simulation_service.create.application.technology.solar.SolarSimulationAssessmentPolicy;
import com.renewsim.backend.simulation_service.create.application.technology.solar.SolarSimulationEngine;
import com.renewsim.backend.simulation_service.create.application.technology.wind.WindSimulationEngine;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationResultSnapshotJacksonWriter;
import com.renewsim.backend.simulation_service.shared.application.SimulationBusinessTelemetry;
import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;
import com.renewsim.backend.simulation_service.shared.application.SimulationTechnologySupport;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import com.renewsim.backend.simulation_service.shared.application.port.out.ScenarioLookupPort;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies that scenario defaults are persisted as immutable simulation
 * snapshots
 * when scenarios change between successive simulation creations.
 */
@ExtendWith(MockitoExtension.class)
class CreateSimulationFromScenarioSnapshotTest {

        @Mock
        private ScenarioLookupPort scenarioLookupPort;
        @Mock
        private TechnologyLookupPort technologyLookupPort;
        @Mock
        private PvgisSolarResourcePort resourcePort;

        @Test
        @DisplayName("persists each scenario's defaults as a snapshot at creation time")
        void persistsScenarioDefaultsAsSnapshotAtCreationTime() {
                JpaSimulationRepository jpaRepository = mock(JpaSimulationRepository.class);
                var persistence = InMemorySimulationRecordRepositorySupport.create(jpaRepository);
                CreateSimulationFromScenarioService service = new CreateSimulationFromScenarioService(
                                scenarioLookupPort,
                                technologyLookupPort,
                                realCreateService(persistence.createRepository()),
                                new ScenarioSimulationCommandFactory(new ScenarioSimulationDefaultsPolicy()));

                when(scenarioLookupPort.findActiveScenarioById(7L)).thenReturn(
                                Optional.of(scenarioSnapshot(5.0, 12000.0, 6000.0)),
                                Optional.of(scenarioSnapshot(8.0, 18000.0, 9000.0)));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(1L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(2L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
                when(technologyLookupPort.recommendActiveTechnologyIdsByEnergyType("solar"))
                                .thenReturn(List.of(1L, 2L));
                when(resourcePort.fetchProfile(37.3891, -5.9845, 13.0)).thenReturn(profile());

                var firstResponse = service.createSimulationFromScenario(command("Simulation 1"));
                service.createSimulationFromScenario(command("Simulation 2"));

                Simulation first = persistence.detailQuery().findById(Long.valueOf(firstResponse.id())).orElseThrow();
                SimulationReadModel secondReadModel = persistence.dashboardQuery()
                                .findByCreatedByOrderByCreatedAtDesc("alice")
                                .stream()
                                .filter(simulation -> "Simulation 2".equals(simulation.name()))
                                .findFirst()
                                .orElseThrow();
                Simulation second = persistence.detailQuery().findById(secondReadModel.id()).orElseThrow();

                assertThat(first.getSystem().installedCapacityKw()).isEqualTo(5.0);
                assertThat(first.getDemand().annualConsumptionKwh()).isEqualTo(6000.0);
                assertThat(first.getEconomics().capexTotal()).isEqualTo(12000.0);
                assertThat(first.getEconomics().currency().value()).isEqualTo("EUR");

                assertThat(second.getSystem().installedCapacityKw()).isEqualTo(8.0);
                assertThat(second.getDemand().annualConsumptionKwh()).isEqualTo(9000.0);
                assertThat(second.getEconomics().capexTotal()).isEqualTo(18000.0);
                assertThat(second.getEconomics().currency().value()).isEqualTo("EUR");
        }

        private CreateRealSimulationUseCase realCreateService(
                        com.renewsim.backend.simulation_service.create.application.port.out.CreateSimulationRepositoryPort repository) {
                var registry = new SimpleMeterRegistry();
                return new CreateSimulationService(
                                repository,
                                new SimulationCompletionAssembler(
                                                new SimulationResultSnapshotJacksonWriter(
                                                                new ObjectMapper().findAndRegisterModules())),
                                new SimulationUseCaseTelemetry(registry),
                                new SimulationBusinessTelemetry(registry),
                                new SimulationTechnologySupport(
                                                technologyLookupPort,
                                                List.of(
                                                                new SolarSimulationEngine(resourcePort,
                                                                                new SolarSimulationAssessmentPolicy()),
                                                                new WindSimulationEngine(),
                                                                new HydroSimulationEngine())));
        }

        private CreateSimulationFromScenarioCommand command(String name) {
                return new CreateSimulationFromScenarioCommand(
                                7L,
                                name,
                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845,
                                                "Spain", CountryCode.of("ES")),
                                "alice");
        }

        private ScenarioLookupPort.ScenarioSnapshot scenarioSnapshot(
                        double capacity, double capex, double consumption) {
                return new ScenarioLookupPort.ScenarioSnapshot(
                                7L, "Hogar solar - Sevilla", 1L,
                                capacity, capex, "EUR", 0.15, consumption);
        }

        private PvgisSolarResourcePort.PvgisSolarResourceProfile profile() {
                return new PvgisSolarResourcePort.PvgisSolarResourceProfile(
                                List.of(117.91, 117.78, 140.16, 142.40, 155.64, 154.71, 164.37, 161.96, 145.57, 132.00,
                                                112.14, 112.69),
                                List.of(144.21, 146.57, 177.98, 185.86, 208.58, 212.34, 230.04, 226.42, 197.02, 172.86,
                                                140.05, 137.50),
                                List.of(10.0, 12.0, 15.0, 17.0, 22.0, 27.0, 31.0, 31.0, 27.0, 21.0, 15.0, 11.0),
                                "2005-2020",
                                "PVGIS");
        }
}

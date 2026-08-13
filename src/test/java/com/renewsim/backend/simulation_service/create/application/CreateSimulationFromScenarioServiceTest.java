package com.renewsim.backend.simulation_service.create.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.scenario_service.domain.exception.ScenarioNotFoundException;
import com.renewsim.backend.simulation_service.create.application.command.CreateSimulationFromScenarioCommand;
import com.renewsim.backend.simulation_service.create.application.port.in.CreateRealSimulationUseCase;
import com.renewsim.backend.simulation_service.create.application.port.out.CreateSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.create.application.technology.hydro.HydroSimulationEngine;
import com.renewsim.backend.simulation_service.create.application.technology.solar.SolarSimulationAssessmentPolicy;
import com.renewsim.backend.simulation_service.create.application.technology.solar.SolarSimulationEngine;
import com.renewsim.backend.simulation_service.create.application.technology.wind.WindSimulationEngine;
import com.renewsim.backend.simulation_service.create.application.port.out.PvgisSolarResourcePort;
import com.renewsim.backend.simulation_service.domain.exception.InvalidConsumptionProfileException;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationCurrencyException;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import com.renewsim.backend.simulation_service.shared.application.port.out.ScenarioLookupPort;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.SimulationId;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationRecordRepositoryAdapter;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationResultSnapshotJacksonWriter;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CreateSimulationFromScenarioServiceTest {

        @Mock
        private ScenarioLookupPort scenarioLookupPort;
        @Mock
        private TechnologyLookupPort technologyLookupPort;
        @Mock
        private CreateSimulationRepositoryPort repository;
        @Mock
        private PvgisSolarResourcePort resourcePort;

        private final ScenarioSimulationCommandFactory scenarioSimulationCommandFactory = new ScenarioSimulationCommandFactory();

        @Test
        @DisplayName("createSimulationFromScenario resolves scenario defaults and delegates to the real flow")
        void createSimulationFromScenarioResolvesScenarioDefaultsAndDelegates() {
                CreateSimulationFromScenarioService service = new CreateSimulationFromScenarioService(
                                scenarioLookupPort,
                                technologyLookupPort,
                                realCreateService(),
                                scenarioSimulationCommandFactory);

                when(scenarioLookupPort.findActiveScenarioById(7L)).thenReturn(Optional.of(scenarioSnapshot()));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(1L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(2L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
                when(technologyLookupPort.recommendActiveTechnologyIdsByEnergyType("solar"))
                                .thenReturn(List.of(1L, 2L));
                when(resourcePort.fetchProfile(37.3891, -5.9845, 13.0)).thenReturn(profile());
                when(repository.save(any())).thenAnswer(invocation -> {
                        Simulation sim = invocation.getArgument(0);
                        if (sim.getId() == null) {
                                sim.assignId(SimulationId.of(60L));
                        }
                        return sim;
                });

                SimulationDetailsResult response = service.createSimulationFromScenario(
                                new CreateSimulationFromScenarioCommand(
                                                7L, null,
                                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845,
                                                                "Spain",
                                                                CountryCode.of("ES")),
                                                "alice"));

                assertThat(response.id()).isEqualTo("60");
                assertThat(response.input().technology()).isEqualTo("solar");

                ArgumentCaptor<Simulation> captor = ArgumentCaptor.forClass(Simulation.class);
                verify(repository, atLeastOnce()).save(captor.capture());
                Simulation persisted = captor.getAllValues().get(captor.getAllValues().size() - 1);
                assertThat(persisted.getScenarioId()).isEqualTo(7L);
                assertThat(persisted.getName()).isEqualTo("Hogar solar - Sevilla");
                assertThat(persisted.getSystem().installedCapacityKw()).isEqualTo(5.0);
                assertThat(persisted.getDemand().annualConsumptionKwh()).isEqualTo(6000.0);
                assertThat(persisted.getEconomics().capexTotal()).isEqualTo(12000.0);
                assertThat(persisted.getTechnologyIds()).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("createSimulationFromScenario keeps request name when provided")
        void createSimulationFromScenarioKeepsRequestNameWhenProvided() {
                CreateSimulationFromScenarioService service = new CreateSimulationFromScenarioService(
                                scenarioLookupPort,
                                technologyLookupPort,
                                realCreateService(),
                                scenarioSimulationCommandFactory);

                when(scenarioLookupPort.findActiveScenarioById(7L)).thenReturn(Optional.of(scenarioSnapshot()));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(1L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(2L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
                when(technologyLookupPort.recommendActiveTechnologyIdsByEnergyType("solar"))
                                .thenReturn(List.of(1L, 2L));
                when(resourcePort.fetchProfile(37.3891, -5.9845, 13.0)).thenReturn(profile());
                when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

                service.createSimulationFromScenario(
                                new CreateSimulationFromScenarioCommand(
                                                7L, "Mi simulacion personalizada",
                                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845,
                                                                "Spain",
                                                                CountryCode.of("ES")),
                                                "alice"));

                ArgumentCaptor<Simulation> captor = ArgumentCaptor.forClass(Simulation.class);
                verify(repository, atLeastOnce()).save(captor.capture());
                assertThat(captor.getAllValues().get(captor.getAllValues().size() - 1).getName())
                                .isEqualTo("Mi simulacion personalizada");
        }

        @Test
        @DisplayName("createSimulationFromScenario snapshots scenario defaults at creation time")
        void createSimulationFromScenarioSnapshotsScenarioDefaultsAtCreationTime() {
                JpaSimulationRepository jpaRepository = mock(JpaSimulationRepository.class);
                SimulationRecordRepositoryAdapter persistenceRepository = inMemoryPersistenceRepository(jpaRepository);
                CreateSimulationFromScenarioService service = new CreateSimulationFromScenarioService(
                                scenarioLookupPort,
                                technologyLookupPort,
                                realCreateService(persistenceRepository),
                                scenarioSimulationCommandFactory);

                when(scenarioLookupPort.findActiveScenarioById(7L)).thenReturn(
                                Optional.of(scenarioSnapshot()),
                                Optional.of(new ScenarioLookupPort.ScenarioSnapshot(
                                                7L, "Hogar solar - Sevilla", 1L,
                                                8.0, 18000.0, "EUR", 0.21, 9000.0)));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(1L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(2L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
                when(technologyLookupPort.recommendActiveTechnologyIdsByEnergyType("solar"))
                                .thenReturn(List.of(1L, 2L));
                when(resourcePort.fetchProfile(37.3891, -5.9845, 13.0)).thenReturn(profile());

                SimulationDetailsResult firstResponse = service.createSimulationFromScenario(
                                new CreateSimulationFromScenarioCommand(
                                                7L, "Simulation 1",
                                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845,
                                                                "Spain",
                                                                CountryCode.of("ES")),
                                                "alice"));
                service.createSimulationFromScenario(
                                new CreateSimulationFromScenarioCommand(
                                                7L, "Simulation 2",
                                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845,
                                                                "Spain",
                                                                CountryCode.of("ES")),
                                                "alice"));

                Simulation firstSimulation = persistenceRepository.findById(Long.valueOf(firstResponse.id()))
                                .orElseThrow();
                Simulation secondSimulation = persistenceRepository.findByCreatedByOrderByCreatedAtDesc("alice")
                                .stream()
                                .filter(simulation -> "Simulation 2".equals(simulation.getName()))
                                .findFirst()
                                .orElseThrow();

                assertThat(firstSimulation.getSystem().installedCapacityKw()).isEqualTo(5.0);
                assertThat(firstSimulation.getDemand().annualConsumptionKwh()).isEqualTo(6000.0);
                assertThat(firstSimulation.getEconomics().capexTotal()).isEqualTo(12000.0);
                assertThat(firstSimulation.getEconomics().currency().value()).isEqualTo("EUR");

                assertThat(secondSimulation.getSystem().installedCapacityKw()).isEqualTo(8.0);
                assertThat(secondSimulation.getDemand().annualConsumptionKwh()).isEqualTo(9000.0);
                assertThat(secondSimulation.getEconomics().capexTotal()).isEqualTo(18000.0);
                assertThat(secondSimulation.getEconomics().currency().value()).isEqualTo("EUR");
        }

        @Test
        @DisplayName("createSimulationFromScenario falls back to the scenario name when request name is blank")
        void createSimulationFromScenarioFallsBackToScenarioNameWhenRequestNameIsBlank() {
                CreateSimulationFromScenarioService service = new CreateSimulationFromScenarioService(
                                scenarioLookupPort,
                                technologyLookupPort,
                                realCreateService(),
                                scenarioSimulationCommandFactory);

                when(scenarioLookupPort.findActiveScenarioById(7L)).thenReturn(Optional.of(scenarioSnapshot()));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(1L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(2L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
                when(technologyLookupPort.recommendActiveTechnologyIdsByEnergyType("solar"))
                                .thenReturn(List.of(1L, 2L));
                when(resourcePort.fetchProfile(37.3891, -5.9845, 13.0)).thenReturn(profile());
                when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

                service.createSimulationFromScenario(
                                new CreateSimulationFromScenarioCommand(
                                                7L, "   ",
                                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845,
                                                                "Spain",
                                                                CountryCode.of("ES")),
                                                "alice"));

                ArgumentCaptor<Simulation> captor = ArgumentCaptor.forClass(Simulation.class);
                verify(repository, atLeastOnce()).save(captor.capture());
                assertThat(captor.getAllValues().get(captor.getAllValues().size() - 1).getName())
                                .isEqualTo("Hogar solar - Sevilla");
        }

        @Test
        @DisplayName("createSimulationFromScenario fails when scenario is missing or inactive")
        void createSimulationFromScenarioFailsWhenScenarioMissing() {
                CreateSimulationFromScenarioService service = new CreateSimulationFromScenarioService(
                                scenarioLookupPort,
                                technologyLookupPort,
                                realCreateService(),
                                scenarioSimulationCommandFactory);

                when(scenarioLookupPort.findActiveScenarioById(99L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.createSimulationFromScenario(
                                new CreateSimulationFromScenarioCommand(
                                                99L, null,
                                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845,
                                                                "Spain",
                                                                CountryCode.of("ES")),
                                                "alice")))
                                .isInstanceOf(ScenarioNotFoundException.class);

                verify(technologyLookupPort, never()).findActiveEnergyTypeByTechnologyId(any());
                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("createSimulationFromScenario fails when the scenario technology is not active")
        void createSimulationFromScenarioFailsWhenScenarioTechnologyIsNotActive() {
                CreateSimulationFromScenarioService service = new CreateSimulationFromScenarioService(
                                scenarioLookupPort,
                                technologyLookupPort,
                                realCreateService(),
                                scenarioSimulationCommandFactory);

                when(scenarioLookupPort.findActiveScenarioById(7L)).thenReturn(Optional.of(scenarioSnapshot()));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(1L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.createSimulationFromScenario(
                                new CreateSimulationFromScenarioCommand(
                                                7L, null,
                                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845,
                                                                "Spain",
                                                                CountryCode.of("ES")),
                                                "alice")))
                                .isInstanceOf(ScenarioNotFoundException.class);

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("createSimulationFromScenario fails when the scenario consumption is not positive")
        void createSimulationFromScenarioFailsWhenScenarioConsumptionIsNotPositive() {
                CreateSimulationFromScenarioService service = new CreateSimulationFromScenarioService(
                                scenarioLookupPort,
                                technologyLookupPort,
                                realCreateService(),
                                scenarioSimulationCommandFactory);

                when(scenarioLookupPort.findActiveScenarioById(7L)).thenReturn(Optional.of(
                                new ScenarioLookupPort.ScenarioSnapshot(7L, "Hogar solar - Sevilla", 1L,
                                                5.0, 12000.0, "EUR", 0.15, 0.0)));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(1L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.recommendActiveTechnologyIdsByEnergyType("solar"))
                                .thenReturn(List.of(1L, 2L));

                assertThatThrownBy(() -> service.createSimulationFromScenario(
                                new CreateSimulationFromScenarioCommand(
                                                7L, null,
                                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845,
                                                                "Spain",
                                                                CountryCode.of("ES")),
                                                "alice")))
                                .isInstanceOf(InvalidConsumptionProfileException.class)
                                .hasMessage("VALIDATION_ERROR: scenario defaultConsumption must be positive");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("createSimulationFromScenario rejects scenario currencies outside the supported simulation contract")
        void createSimulationFromScenarioRejectsUnsupportedScenarioCurrency() {
                CreateSimulationFromScenarioService service = new CreateSimulationFromScenarioService(
                                scenarioLookupPort,
                                technologyLookupPort,
                                realCreateService(),
                                scenarioSimulationCommandFactory);

                when(scenarioLookupPort.findActiveScenarioById(7L)).thenReturn(Optional.of(
                                new ScenarioLookupPort.ScenarioSnapshot(7L, "Hogar solar - Sevilla", 1L,
                                                5.0, 12000.0, "USD", 0.15, 6000.0)));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(1L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.recommendActiveTechnologyIdsByEnergyType("solar"))
                                .thenReturn(List.of(1L, 2L));

                assertThatThrownBy(() -> service.createSimulationFromScenario(
                                new CreateSimulationFromScenarioCommand(
                                                7L, null,
                                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845,
                                                                "Spain", CountryCode.of("ES")),
                                                "alice")))
                                .isInstanceOf(InvalidSimulationCurrencyException.class)
                                .hasMessage("VALIDATION_ERROR: scenario defaultInvestmentCurrency must be EUR");
        }

        @Test
        @DisplayName("createSimulationFromScenario accepts supported scenario currency with surrounding whitespace")
        void createSimulationFromScenarioAcceptsSupportedScenarioCurrencyWithWhitespace() {
                CreateSimulationFromScenarioService service = new CreateSimulationFromScenarioService(
                                scenarioLookupPort,
                                technologyLookupPort,
                                realCreateService(),
                                scenarioSimulationCommandFactory);

                when(scenarioLookupPort.findActiveScenarioById(7L)).thenReturn(Optional.of(
                                new ScenarioLookupPort.ScenarioSnapshot(7L, "Hogar solar - Sevilla", 1L,
                                                5.0, 12000.0, " EUR ", 0.15, 6000.0)));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(1L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(2L)).thenReturn(Optional.of("solar"));
                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
                when(technologyLookupPort.recommendActiveTechnologyIdsByEnergyType("solar"))
                                .thenReturn(List.of(1L, 2L));
                when(resourcePort.fetchProfile(37.3891, -5.9845, 13.0)).thenReturn(profile());
                when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

                service.createSimulationFromScenario(
                                new CreateSimulationFromScenarioCommand(
                                                7L, null,
                                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845,
                                                                "Spain", CountryCode.of("ES")),
                                                "alice"));

                ArgumentCaptor<Simulation> captor = ArgumentCaptor.forClass(Simulation.class);
                verify(repository, atLeastOnce()).save(captor.capture());
                assertThat(captor.getAllValues().get(captor.getAllValues().size() - 1).getEconomics().currency().value())
                                .isEqualTo("EUR");
        }

        private CreateRealSimulationUseCase realCreateService() {
                return realCreateService(repository);
        }

        private CreateRealSimulationUseCase realCreateService(CreateSimulationRepositoryPort simulationRepository) {
                return new CreateSimulationService(
                                simulationRepository,
                                technologyLookupPort,
                                List.of(
                                                new SolarSimulationEngine(resourcePort,
                                                                new SolarSimulationAssessmentPolicy()),
                                                new WindSimulationEngine(),
                                                new HydroSimulationEngine()),
                                new SimulationCompletionAssembler(
                                                new SimulationResultSnapshotJacksonWriter(
                                                                new ObjectMapper().findAndRegisterModules())),
                                new SimulationUseCaseTelemetry(new SimpleMeterRegistry()));
        }

        private SimulationRecordRepositoryAdapter inMemoryPersistenceRepository(JpaSimulationRepository jpaRepository) {
                Map<Long, SimulationEntity> store = new HashMap<>();
                AtomicLong sequence = new AtomicLong(60L);

                when(jpaRepository.save(any(SimulationEntity.class))).thenAnswer(invocation -> {
                        SimulationEntity entity = invocation.getArgument(0);
                        SimulationEntity stored = copyEntity(entity);
                        if (stored.getId() == null) {
                                stored.setId(sequence.getAndIncrement());
                        }
                        store.put(stored.getId(), stored);
                        return copyEntity(stored);
                });
                when(jpaRepository.findById(any(Long.class))).thenAnswer(invocation -> {
                        Long id = invocation.getArgument(0);
                        SimulationEntity stored = store.get(id);
                        return Optional.ofNullable(stored == null ? null : copyEntity(stored));
                });
                when(jpaRepository.findByCreatedByOrderByCreatedAtDesc("alice")).thenAnswer(invocation -> store.values()
                                .stream()
                                .filter(entity -> "alice".equals(entity.getCreatedBy()))
                                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                                .map(this::copyEntity)
                                .toList());

                return new SimulationRecordRepositoryAdapter(jpaRepository,
                                new ObjectMapper().findAndRegisterModules());
        }

        private SimulationEntity copyEntity(SimulationEntity source) {
                SimulationEntity copy = new SimulationEntity();
                copy.setId(source.getId());
                copy.setName(source.getName());
                copy.setLocation(source.getLocation());
                copy.setEnergyType(source.getEnergyType());
                copy.setLocationLat(source.getLocationLat());
                copy.setLocationLng(source.getLocationLng());
                copy.setProjectSize(source.getProjectSize());
                copy.setBudget(source.getBudget());
                copy.setEstimatedEnergy(source.getEstimatedEnergy());
                copy.setClimateData(source.getClimateData());
                copy.setCreatedBy(source.getCreatedBy());
                copy.setCreatedAt(source.getCreatedAt());
                copy.setUpdatedAt(source.getUpdatedAt());
                copy.setStatus(source.getStatus());
                copy.setAnnualSavings(source.getAnnualSavings());
                copy.setNpv(source.getNpv());
                copy.setIrrPct(source.getIrrPct());
                copy.setRecommendation(source.getRecommendation());
                copy.setScenarioId(source.getScenarioId());
                copy.setInputSnapshot(source.getInputSnapshot());
                copy.setResultSnapshot(source.getResultSnapshot());
                copy.setTechnologyIds(
                                source.getTechnologyIds() == null ? List.of() : List.copyOf(source.getTechnologyIds()));
                return copy;
        }

        private ScenarioLookupPort.ScenarioSnapshot scenarioSnapshot() {
                return new ScenarioLookupPort.ScenarioSnapshot(
                                7L, "Hogar solar - Sevilla", 1L,
                                5.0, 12000.0, "EUR", 0.15, 6000.0);
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

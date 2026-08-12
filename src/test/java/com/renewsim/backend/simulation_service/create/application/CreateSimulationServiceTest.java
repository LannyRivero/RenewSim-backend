package com.renewsim.backend.simulation_service.create.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.shared.exception.BadRequestException;
import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.port.out.CreateSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.create.application.port.out.PvgisSolarResourcePort;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.SimulationId;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationResultSnapshotJacksonWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.renewsim.backend.simulation_service.support.SimulationCreateTestFixtures.engines;
import static com.renewsim.backend.simulation_service.support.SimulationCreateTestFixtures.profile;
import static com.renewsim.backend.simulation_service.support.SimulationCreateTestFixtures.validCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSimulationServiceTest {

        @Mock
        private CreateSimulationRepositoryPort repository;
        @Mock
        private PvgisSolarResourcePort resourcePort;
        @Mock
        private TechnologyLookupPort technologyLookupPort;

        @Test
        @DisplayName("createSimulation computes and stores the real contract snapshots")
        void createSimulationComputesAndStoresRealContractSnapshots() {
                CreateSimulationService service = newCreateSimulationService();
                CreateRealSimulationCommand command = validCommand();

                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
                when(technologyLookupPort.recommendActiveTechnologyIdsByEnergyType("solar"))
                                .thenReturn(List.of(1L, 2L));
                when(resourcePort.fetchProfile(37.3891, -5.9845, 13.0)).thenReturn(profile());
                when(repository.save(any())).thenAnswer(invocation -> {
                        Simulation sim = invocation.getArgument(0);
                        if (sim.getId() == null) {
                                sim.assignId(SimulationId.of(55L));
                        }
                        return sim;
                });

                SimulationDetailsResult response = service.createSimulation(command);

                assertThat(response.id()).isEqualTo("55");
                assertThat(response.modelVersion()).isEqualTo("solar-spain-v1");
                assertThat(response.technical().annualGenerationKwh()).isGreaterThan(400000);

                ArgumentCaptor<Simulation> captor = ArgumentCaptor.forClass(Simulation.class);
                verify(repository, times(2)).save(captor.capture());
                assertThat(captor.getAllValues().get(1).getResultSnapshot()).isNotBlank();
                assertThat(captor.getAllValues().get(1).getTechnologyIds()).containsExactly(1L, 2L);
                verify(technologyLookupPort).recommendActiveTechnologyIdsByEnergyType("solar");
        }

        @Test
        @DisplayName("createSimulation rejects not-yet-implemented wind before persisting a draft")
        void createSimulationRejectsNotImplementedWindBeforePersisting() {
                CreateSimulationService service = newCreateSimulationService();

                CreateRealSimulationCommand command = new CreateRealSimulationCommand(
                                validCommand().name(),
                                Technology.of("wind"),
                                validCommand().location(),
                                validCommand().system(),
                                validCommand().demand(),
                                validCommand().economics(),
                                validCommand().technologyIds(),
                                validCommand().scenarioId(),
                                validCommand().createdBy());

                when(technologyLookupPort.existsActiveByEnergyType("wind")).thenReturn(true);

                assertThatThrownBy(() -> service.createSimulation(command))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessage("UNSUPPORTED_TECHNOLOGY: 'wind' simulation is not implemented yet");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("createSimulation rejects caller supplied technology ids that are inactive")
        void createSimulationRejectsCallerSuppliedTechnologyIdsThatAreInactive() {
                CreateSimulationService service = newCreateSimulationService();

                CreateRealSimulationCommand command = new CreateRealSimulationCommand(
                                validCommand().name(),
                                validCommand().technology(),
                                validCommand().location(),
                                validCommand().system(),
                                validCommand().demand(),
                                validCommand().economics(),
                                List.of(99L),
                                null,
                                validCommand().createdBy());

                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(99L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.createSimulation(command))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessage("UNSUPPORTED_TECHNOLOGY_ID: '99' is not registered or is inactive in the technology catalog");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("createSimulation rejects caller supplied technology ids that do not match the simulation energy type")
        void createSimulationRejectsCallerSuppliedTechnologyIdsThatDoNotMatchTheSimulationEnergyType() {
                CreateSimulationService service = newCreateSimulationService();

                CreateRealSimulationCommand command = new CreateRealSimulationCommand(
                                validCommand().name(),
                                validCommand().technology(),
                                validCommand().location(),
                                validCommand().system(),
                                validCommand().demand(),
                                validCommand().economics(),
                                List.of(15L),
                                null,
                                validCommand().createdBy());

                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(15L)).thenReturn(Optional.of("wind"));

                assertThatThrownBy(() -> service.createSimulation(command))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessage("INCOMPATIBLE_TECHNOLOGY_ID: '15' does not belong to energyType 'solar'");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("createSimulation rejects caller supplied technology ids that are duplicated")
        void createSimulationRejectsCallerSuppliedTechnologyIdsThatAreDuplicated() {
                CreateSimulationService service = newCreateSimulationService();

                CreateRealSimulationCommand command = new CreateRealSimulationCommand(
                                validCommand().name(),
                                validCommand().technology(),
                                validCommand().location(),
                                validCommand().system(),
                                validCommand().demand(),
                                validCommand().economics(),
                                List.of(11L, 11L),
                                null,
                                validCommand().createdBy());

                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);

                assertThatThrownBy(() -> service.createSimulation(command))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessage("DUPLICATE_TECHNOLOGY_IDS: technologyIds must not contain duplicates");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("ConsumptionProfile rejects annual demand mismatches at domain level")
        void consumptionProfileRejectsAnnualDemandMismatch() {
                assertThatThrownBy(() -> ConsumptionProfile.of(999999,
                                List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                                10000d, 10000d)))
                                .isInstanceOf(BadRequestException.class);
        }

        private CreateSimulationService newCreateSimulationService() {
                ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
                return new CreateSimulationService(
                                repository,
                                technologyLookupPort,
                                engines(resourcePort),
                                new SimulationCompletionAssembler(
                                                new SimulationResultSnapshotJacksonWriter(objectMapper)));
        }
}

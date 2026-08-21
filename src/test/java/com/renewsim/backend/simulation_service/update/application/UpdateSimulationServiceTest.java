package com.renewsim.backend.simulation_service.update.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.create.application.SimulationCompletionAssembler;
import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.port.out.CreateSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.create.application.port.out.PvgisSolarResourcePort;
import com.renewsim.backend.simulation_service.detail.application.port.out.SimulationDetailQueryPort;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationStatusTransitionException;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.SimulationStatus;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.Currency;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectLifetime;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationResultSnapshotJacksonWriter;
import com.renewsim.backend.simulation_service.shared.application.SimulationBusinessTelemetry;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.SimulationTechnologySupport;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.simulation_service.update.application.command.UpdateSimulationCommand;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.renewsim.backend.shared.exception.ForbiddenException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.renewsim.backend.simulation_service.support.SimulationCreateTestFixtures.engines;
import static com.renewsim.backend.simulation_service.support.SimulationCreateTestFixtures.profile;
import static com.renewsim.backend.simulation_service.support.SimulationCreateTestFixtures.validCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSimulationServiceTest {

    @Mock
    private SimulationDetailQueryPort detailQueryPort;
    @Mock
    private CreateSimulationRepositoryPort repository;
    @Mock
    private PvgisSolarResourcePort resourcePort;
    @Mock
    private TechnologyLookupPort technologyLookupPort;

    @Test
    @DisplayName("updateSimulation recomputes and saves an edited simulation preserving identity")
    void updateSimulationRecomputesAndSavesEditedSimulation() {
        UpdateSimulationService service = newUpdateSimulationService();
        UpdateSimulationCommand command = validUpdateCommand();

        when(detailQueryPort.findById(55L)).thenReturn(Optional.of(existingSimulation()));
        when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
        when(technologyLookupPort.recommendActiveTechnologyIdsByEnergyType("solar")).thenReturn(List.of(1L, 2L));
        when(resourcePort.fetchProfile(37.3891, -5.9845, 13.0)).thenReturn(profile());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SimulationDetailsResult response = service.updateSimulation(command, "alice", false);

        assertThat(response.id()).isEqualTo("55");
        assertThat(response.modelVersion()).isEqualTo("solar-spain-v1");
        assertThat(response.technical().annualGenerationKwh()).isGreaterThan(400000);

        ArgumentCaptor<Simulation> captor = ArgumentCaptor.forClass(Simulation.class);
        verify(repository).save(captor.capture());
        Simulation saved = captor.getValue();
        assertThat(saved.getId().value()).isEqualTo(55L);
        assertThat(saved.getCreatedBy()).isEqualTo("alice");
        assertThat(saved.getStatus()).isEqualTo(SimulationStatus.COMPLETED);
        assertThat(saved.getResultSnapshot()).isNotBlank();
        assertThat(saved.getTechnologyIds()).containsExactly(1L, 2L);

        verify(detailQueryPort).findById(55L);
        assertThat(meterRegistry()
                .counter("simulation_service_use_case_total", "use_case", "update", "outcome", "success").count())
                .isEqualTo(1.0d);
        assertThat(meterRegistry().counter("simulation_service_business_recommendation_total", "technology", "solar",
                "recommendation", response.summary().recommendation()).count())
                .isEqualTo(1.0d);
    }

    @Test
    @DisplayName("updateSimulation rejects a simulation that does not exist")
    void updateSimulationRejectsMissingSimulation() {
        UpdateSimulationService service = newUpdateSimulationService();

        when(detailQueryPort.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSimulation(validUpdateCommand(), "alice", false))
                .isInstanceOf(SimulationNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("updateSimulation rejects a requester that is not the owner and not admin")
    void updateSimulationRejectsNotOwner() {
        UpdateSimulationService service = newUpdateSimulationService();

        when(detailQueryPort.findById(55L)).thenReturn(Optional.of(existingSimulation()));

        assertThatThrownBy(() -> service.updateSimulation(validUpdateCommand(), "mallory", false))
                .isInstanceOf(ForbiddenException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("updateSimulation allows an admin to edit a simulation owned by someone else")
    void updateSimulationAllowsAdminToEditForeignSimulation() {
        UpdateSimulationService service = newUpdateSimulationService();

        when(detailQueryPort.findById(55L)).thenReturn(Optional.of(existingSimulation()));
        when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
        when(technologyLookupPort.recommendActiveTechnologyIdsByEnergyType("solar")).thenReturn(List.of(1L, 2L));
        when(resourcePort.fetchProfile(37.3891, -5.9845, 13.0)).thenReturn(profile());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SimulationDetailsResult response = service.updateSimulation(validUpdateCommand(), "admin", true);

        assertThat(response.id()).isEqualTo("55");
        verify(repository).save(any());
    }

    @Test
    @DisplayName("updateSimulation rejects a deleted simulation")
    void updateSimulationRejectsDeletedSimulation() {
        UpdateSimulationService service = newUpdateSimulationService();

        when(detailQueryPort.findById(55L)).thenReturn(Optional.of(existingSimulation(SimulationStatus.DELETED)));

        assertThatThrownBy(() -> service.updateSimulation(validUpdateCommand(), "alice", false))
                .isInstanceOf(InvalidSimulationStatusTransitionException.class);

        verify(repository, never()).save(any());
    }

    private UpdateSimulationService newUpdateSimulationService() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        return new UpdateSimulationService(
                detailQueryPort,
                repository,
                new SimulationCompletionAssembler(
                        new SimulationResultSnapshotJacksonWriter(objectMapper)),
                new SimulationUseCaseTelemetry(meterRegistry()),
                new SimulationBusinessTelemetry(meterRegistry()),
                new SimulationTechnologySupport(technologyLookupPort, engines(resourcePort)));
    }

    private UpdateSimulationCommand validUpdateCommand() {
        CreateRealSimulationCommand create = validCommand();
        return new UpdateSimulationCommand(
                55L,
                create.name(),
                create.technology(),
                create.location(),
                create.system(),
                create.demand(),
                create.economics(),
                create.technologyIds());
    }

    private Simulation existingSimulation() {
        return existingSimulation(SimulationStatus.COMPLETED);
    }

    private Simulation existingSimulation(SimulationStatus status) {
        return Simulation.reconstitute(
                55L,
                "Solar - Sevilla",
                Technology.solar(),
                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", CountryCode.of("ES")),
                new SimulationSystem(300, 0.81, 0.5, 99, new SimulationSystem.LossesPct(2, 6, 1, 3, 1)),
                ConsumptionProfile.of(120000,
                        List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                10000d, 10000d)),
                new SimulationEconomics(Currency.of("EUR"), 315000, 7200, 0.18, 0.07, 8, ProjectLifetime.of(20)),
                status,
                "{\"old\":true}",
                1000.0,
                100.0,
                50.0,
                5.0,
                "old",
                List.of(11L, 12L),
                null,
                "alice",
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private SimpleMeterRegistry meterRegistry() {
        return registry;
    }

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
}

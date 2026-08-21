package com.renewsim.backend.simulation_service.delete.application;

import com.renewsim.backend.shared.exception.ForbiddenException;
import com.renewsim.backend.simulation_service.delete.application.port.out.DeleteSimulationRepositoryPort;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteSimulationServiceTest {

    @Mock
    private DeleteSimulationRepositoryPort repository;

    @Test
    @DisplayName("deleteSimulation persists the DELETED status instead of hard deleting the row")
    void deleteSimulationPersistsDeletedStatus() {
        DeleteSimulationService service = new DeleteSimulationService(repository);
        Simulation simulation = existingSimulation(55L, "alice", SimulationStatus.COMPLETED);

        when(repository.findById(55L)).thenReturn(Optional.of(simulation));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.deleteSimulation(55L, "alice", false);

        ArgumentCaptor<Simulation> captor = ArgumentCaptor.forClass(Simulation.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SimulationStatus.DELETED);
    }

    @Test
    @DisplayName("deleteSimulation rejects a requester that is not the owner and not admin")
    void deleteSimulationRejectsNotOwner() {
        DeleteSimulationService service = new DeleteSimulationService(repository);

        when(repository.findById(55L))
                .thenReturn(Optional.of(existingSimulation(55L, "alice", SimulationStatus.COMPLETED)));

        assertThatThrownBy(() -> service.deleteSimulation(55L, "mallory", false))
                .isInstanceOf(ForbiddenException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deleteSimulation rejects missing simulations")
    void deleteSimulationRejectsMissingSimulation() {
        DeleteSimulationService service = new DeleteSimulationService(repository);

        when(repository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteSimulation(55L, "alice", false))
                .isInstanceOf(SimulationNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deleteSimulation rejects simulations already deleted")
    void deleteSimulationRejectsAlreadyDeletedSimulation() {
        DeleteSimulationService service = new DeleteSimulationService(repository);

        when(repository.findById(55L))
                .thenReturn(Optional.of(existingSimulation(55L, "alice", SimulationStatus.DELETED)));

        assertThatThrownBy(() -> service.deleteSimulation(55L, "alice", false))
                .isInstanceOf(InvalidSimulationStatusTransitionException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deleteAllUserSimulations soft deletes every active simulation of the user")
    void deleteAllUserSimulationsSoftDeletesEveryActiveSimulation() {
        DeleteSimulationService service = new DeleteSimulationService(repository);
        Simulation first = existingSimulation(55L, "alice", SimulationStatus.COMPLETED);
        Simulation second = existingSimulation(56L, "alice", SimulationStatus.DRAFT);

        when(repository.findActiveByCreatedBy("alice")).thenReturn(List.of(first, second));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.deleteAllUserSimulations("alice");

        verify(repository, times(2)).save(any(Simulation.class));
        assertThat(first.getStatus()).isEqualTo(SimulationStatus.DELETED);
        assertThat(second.getStatus()).isEqualTo(SimulationStatus.DELETED);
    }

    private Simulation existingSimulation(Long id, String owner, SimulationStatus status) {
        return Simulation.reconstitute(
                id,
                "Solar - Sevilla",
                Technology.solar(),
                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", CountryCode.of("ES")),
                new SimulationSystem(300.0, 0.81, 0.5, 99.0, new SimulationSystem.LossesPct(2.0, 6.0, 1.0, 3.0, 1.0)),
                ConsumptionProfile.of(120000,
                        List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                10000d, 10000d)),
                new SimulationEconomics(Currency.of("EUR"), 315000.0, 7200.0, 0.18, 0.07, 8, ProjectLifetime.of(20)),
                status,
                "{}",
                457200.0,
                68700.0,
                121500.0,
                11.4,
                "viable_with_reservations",
                List.of(11L, 12L),
                null,
                owner,
                LocalDateTime.parse("2026-06-30T14:00:00"),
                LocalDateTime.parse("2026-06-30T14:30:00"));
    }
}

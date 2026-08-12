package com.renewsim.backend.simulation_service.detail.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.detail.application.port.out.SimulationDetailQueryPort;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static com.renewsim.backend.simulation_service.support.SimulationServiceTestFixtures.completedSimulation;
import static com.renewsim.backend.simulation_service.support.SimulationServiceTestFixtures.minimalResult;
import static com.renewsim.backend.simulation_service.support.SimulationServiceTestFixtures.snapshotReader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSimulationServiceTest {

    @Mock
    private SimulationDetailQueryPort repository;

    @Test
    @DisplayName("getSimulationById enforces ownership for non admins")
    void getSimulationByIdEnforcesOwnership() throws Exception {
        GetSimulationService service = new GetSimulationService(repository, snapshotReader());
        SimulationDetailsResult result = minimalResult();
        var simulation = completedSimulation(55L, "alice",
                new ObjectMapper().findAndRegisterModules().writeValueAsString(result));
        when(repository.findById(55L)).thenReturn(Optional.of(simulation));

        assertThatThrownBy(() -> service.getSimulationById(55L, "bob", false))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(service.getSimulationById(55L, "alice", false).id()).isEqualTo("55");
    }

    @Test
    @DisplayName("getSimulationById fails when snapshot is missing")
    void getSimulationByIdFailsWhenSnapshotMissing() {
        GetSimulationService service = new GetSimulationService(repository, snapshotReader());
        var simulation = completedSimulation(55L, "alice", null);
        when(repository.findById(55L)).thenReturn(Optional.of(simulation));

        assertThatThrownBy(() -> service.getSimulationById(55L, "alice", false))
                .isInstanceOf(SimulationNotFoundException.class);
    }
}

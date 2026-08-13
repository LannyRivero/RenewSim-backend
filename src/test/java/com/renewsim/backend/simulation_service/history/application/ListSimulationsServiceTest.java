package com.renewsim.backend.simulation_service.history.application;

import com.renewsim.backend.simulation_service.history.application.port.out.SimulationHistoryQueryPort;
import com.renewsim.backend.simulation_service.history.application.result.UserSimulationListResult;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.renewsim.backend.simulation_service.support.SimulationDetailTestFixtures.completedSimulation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListSimulationsServiceTest {

    @Mock
    private SimulationHistoryQueryPort repository;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Test
    @DisplayName("getUserSimulations maps stored summary columns for scenario-created simulations")
    void getUserSimulationsMapsStoredSummaryColumnsForScenarioCreatedSimulations() {
        ListSimulationsService service = new ListSimulationsService(repository, new SimulationUseCaseTelemetry(meterRegistry));
        var simulation = completedSimulation(55L, "alice", null);
        simulation.assignScenarioId(42L);
        simulation.assignTechnologyIds(List.of(11L, 12L));
        when(repository.findByCreatedByOrderByCreatedAtDesc("alice")).thenReturn(List.of(simulation));

        UserSimulationListResult response = service.getUserSimulations("alice");

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.items().getFirst().annualSavings()).isEqualTo(68700.0);
        assertThat(response.items().getFirst().technology()).isEqualTo("solar");
    }
}

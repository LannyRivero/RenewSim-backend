package com.renewsim.backend.simulation_service.history.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.history.application.port.out.SimulationHistoryQueryPort;
import com.renewsim.backend.simulation_service.history.application.result.UserSimulationListResult;
import com.renewsim.backend.simulation_service.shared.application.port.out.SimulationResultSnapshotReaderPort;
import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationResultSnapshotJacksonReader;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.renewsim.backend.simulation_service.support.SimulationDetailTestFixtures.completedSimulation;
import static com.renewsim.backend.simulation_service.support.SimulationDetailTestFixtures.minimalResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListSimulationsServiceTest {

    @Mock
    private SimulationHistoryQueryPort repository;

    private final SimulationResultSnapshotReaderPort snapshotReader = new SimulationResultSnapshotJacksonReader(
            new ObjectMapper().findAndRegisterModules());

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Test
    @DisplayName("getUserSimulations maps stored summary columns for scenario-created simulations")
    void getUserSimulationsMapsStoredSummaryColumnsForScenarioCreatedSimulations() {
        ListSimulationsService service = new ListSimulationsService(repository,
                snapshotReader,
                new SimulationUseCaseTelemetry(meterRegistry));
        String snapshot;
        try {
            snapshot = new ObjectMapper().findAndRegisterModules().writeValueAsString(minimalResult());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        var simulation = completedSimulation(55L, "alice", snapshot);
        simulation.assignScenarioId(42L);
        simulation.assignTechnologyIds(List.of(11L, 12L));
        when(repository.findByCreatedByOrderByCreatedAtDesc("alice")).thenReturn(List.of(new SimulationReadModel(
                simulation.getId().value(),
                simulation.getName(),
                simulation.getTechnology().value(),
                simulation.getStatus().name(),
                simulation.getLocation().label(),
                simulation.getAnnualGenerationKwh(),
                simulation.getAnnualSavings(),
                simulation.getNpv(),
                simulation.getIrrPct(),
                simulation.getRecommendation(),
                simulation.getEconomics().capexTotal(),
                simulation.getResultSnapshot(),
                simulation.getCreatedAt())));

        UserSimulationListResult response = service.getUserSimulations("alice");

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.items().getFirst().annualSavings()).isEqualTo(68700.0);
        assertThat(response.items().getFirst().technology()).isEqualTo("solar");
        assertThat(response.items().getFirst().modelVersion()).isEqualTo("solar-spain-v1");
        assertThat(response.items().getFirst().resourceSource()).isEqualTo("PVGIS");
    }
}

package com.renewsim.backend.simulation_service.history.web;

import com.renewsim.backend.simulation_service.application.historySimulation.SimulationHistoryRowResult;
import com.renewsim.backend.simulation_service.application.historySimulation.UserSimulationListResult;
import com.renewsim.backend.simulation_service.history.web.dto.ListUserSimulationsResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationHistoryWebMapperTest {

    private final SimulationHistoryWebMapper mapper = new SimulationHistoryWebMapper();

    @Test
    @DisplayName("toWebList preserves history projection")
    void toWebListPreservesHistoryProjection() {
        ListUserSimulationsResponseDTO listResponse = mapper.toWebList(new UserSimulationListResult(
                List.of(new SimulationHistoryRowResult("55", "Solar - Sevilla", "solar", "completed",
                        "2026-06-30T14:00:00Z", "Sevilla", 457200, 68700, 121500, 11.4, "viable_with_reservations",
                        "solar-spain-v1", "PVGIS")),
                1));

        assertThat(listResponse.total()).isEqualTo(1);
        assertThat(listResponse.items().getFirst().technology()).isEqualTo("solar");
    }
}

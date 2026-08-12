package com.renewsim.backend.simulation_service.history.application;

import com.renewsim.backend.simulation_service.history.application.port.in.ListUserRealSimulationsUseCase;
import com.renewsim.backend.simulation_service.history.application.port.out.SimulationHistoryQueryPort;
import com.renewsim.backend.simulation_service.history.application.result.SimulationHistoryRowResult;
import com.renewsim.backend.simulation_service.history.application.result.UserSimulationListResult;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.renewsim.backend.simulation_service.shared.application.SimulationFormatUtils.formatDate;
import static com.renewsim.backend.simulation_service.shared.application.SimulationNumericUtils.defaultNumber;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListSimulationsService implements ListUserRealSimulationsUseCase {

    private static final String MODEL_VERSION = "solar-spain-v1";
    private static final String RESOURCE_SOURCE = "PVGIS";

    private final SimulationHistoryQueryPort repository;

    @Override
    public UserSimulationListResult getUserSimulations(String username) {
        List<SimulationHistoryRowResult> items = repository.findByCreatedByOrderByCreatedAtDesc(username)
                .stream()
                .map(this::toHistoryRow)
                .toList();
        return new UserSimulationListResult(items, items.size());
    }

    private SimulationHistoryRowResult toHistoryRow(Simulation simulation) {
        return new SimulationHistoryRowResult(
                String.valueOf(simulation.getId()),
                simulation.getName(),
                simulation.getTechnology() != null ? simulation.getTechnology().value() : null,
                simulation.getStatus() != null ? simulation.getStatus().name().toLowerCase() : "completed",
                formatDate(simulation.getCreatedAt()),
                simulation.getLocation().label(),
                defaultNumber(simulation.getAnnualGenerationKwh()),
                defaultNumber(simulation.getAnnualSavings()),
                defaultNumber(simulation.getNpv()),
                simulation.getIrrPct(),
                simulation.getRecommendation(),
                MODEL_VERSION,
                RESOURCE_SOURCE);
    }
}

package com.renewsim.backend.simulation_service.history.web;

import com.renewsim.backend.simulation_service.application.historySimulation.UserSimulationListResult;
import com.renewsim.backend.simulation_service.history.web.dto.ListUserSimulationsResponseDTO;
import com.renewsim.backend.simulation_service.history.web.dto.SimulationHistoryRowDTO;

public final class SimulationHistoryWebMapper {

    public ListUserSimulationsResponseDTO toWebList(UserSimulationListResult result) {
        return new ListUserSimulationsResponseDTO(
                result.items().stream()
                        .map(item -> new SimulationHistoryRowDTO(
                                item.id(),
                                item.name(),
                                item.technology(),
                                item.status(),
                                item.createdAt(),
                                item.locationLabel(),
                                item.annualGenerationKwh(),
                                item.annualSavings(),
                                item.npv(),
                                item.irrPct(),
                                item.recommendation(),
                                item.modelVersion(),
                                item.resourceSource()))
                        .toList(),
                result.total());
    }
}

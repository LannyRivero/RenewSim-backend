package com.renewsim.backend.simulation_service.shared.web;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.web.dto.SimulationDetailsResponseDTO;

final class SimulationSummaryWebMapper {

        SimulationDetailsResponseDTO.SummaryDTO toSummary(SimulationDetailsResult.Summary summary) {
                return new SimulationDetailsResponseDTO.SummaryDTO(
                                summary.recommendation(),
                                summary.headline(),
                                summary.summary(),
                                summary.reasons().stream()
                                                .map(reason -> new SimulationDetailsResponseDTO.RecommendationReasonDTO(
                                                                reason.area(),
                                                                reason.severity(),
                                                                reason.message()))
                                                .toList());
        }
}

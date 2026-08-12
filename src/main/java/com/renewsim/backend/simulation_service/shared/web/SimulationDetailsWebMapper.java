package com.renewsim.backend.simulation_service.shared.web;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.web.dto.SimulationDetailsResponseDTO;

public final class SimulationDetailsWebMapper {

        private final SimulationSummaryWebMapper summaryWebMapper = new SimulationSummaryWebMapper();
        private final SimulationInputSnapshotWebMapper inputSnapshotWebMapper = new SimulationInputSnapshotWebMapper();
        private final SimulationTechnicalWebMapper technicalWebMapper = new SimulationTechnicalWebMapper();
        private final SimulationFinancialWebMapper financialWebMapper = new SimulationFinancialWebMapper();

        public SimulationDetailsResponseDTO toWebDetails(SimulationDetailsResult result) {
                return new SimulationDetailsResponseDTO(
                                result.id(),
                                result.status(),
                                result.createdAt(),
                                result.updatedAt(),
                                result.modelVersion(),
                                result.technology(),
                                toResolvedLocation(result),
                                summaryWebMapper.toSummary(result.summary()),
                                inputSnapshotWebMapper.toInput(result.input()),
                                technicalWebMapper.toTechnical(result.technical()),
                                financialWebMapper.toFinancial(result.financial()),
                                financialWebMapper.toAssumptions(result.assumptions()),
                                financialWebMapper.toWarnings(result.warnings()));
        }

        private SimulationDetailsResponseDTO.ResolvedLocationDTO toResolvedLocation(SimulationDetailsResult result) {
                return new SimulationDetailsResponseDTO.ResolvedLocationDTO(
                                result.location().label(),
                                result.location().name(),
                                result.location().adminRegion(),
                                result.location().country(),
                                result.location().countryCode(),
                                result.location().lat(),
                                result.location().lon(),
                                result.location().timezone());
        }

}

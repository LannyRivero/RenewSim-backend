package com.renewsim.backend.simulation_service.web.dto;

import java.util.List;

public record PortfolioDashboardRecommendedScenarioDTO(
                String id,
                String name,
                String technology,
                String location,
                Double roiPercent,
                Double paybackYears,
                Double capex,
                Double estimatedAnnualSavings,
                String priority,
                String headline,
                List<String> drivers,
                String mainRisk,
                String nextStep) {
}

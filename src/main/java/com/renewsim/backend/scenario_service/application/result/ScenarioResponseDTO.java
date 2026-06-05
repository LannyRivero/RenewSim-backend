package com.renewsim.backend.scenario_service.application.result;

import com.renewsim.backend.shared.domain.vo.ClimateData;
import com.renewsim.backend.shared.domain.vo.Money;

public record ScenarioResponseDTO(
        Long id,
        String name,
        String description,
        Long technologyId,
        double defaultCapacityKw,
        Money defaultInvestment,
        double defaultTariff,
        double defaultConsumption,
        ClimateData climateProfile,
        boolean isActive) {
}

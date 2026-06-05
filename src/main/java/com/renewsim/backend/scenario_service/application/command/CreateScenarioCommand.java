package com.renewsim.backend.scenario_service.application.command;

import com.renewsim.backend.scenario_service.domain.model.vo.DefaultCapacityKw;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultConsumption;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultTariff;
import com.renewsim.backend.scenario_service.domain.model.vo.ScenarioTechnologyId;
import com.renewsim.backend.shared.domain.vo.ClimateData;
import com.renewsim.backend.shared.domain.vo.Money;

public record CreateScenarioCommand(
        String name,
        String description,
        ScenarioTechnologyId technologyId,
        DefaultCapacityKw defaultCapacityKw,
        Money defaultInvestment,
        DefaultTariff defaultTariff,
        DefaultConsumption defaultConsumption,
        ClimateData climateProfile) {
}

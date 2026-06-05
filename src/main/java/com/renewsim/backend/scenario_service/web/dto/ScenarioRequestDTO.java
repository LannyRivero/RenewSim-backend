package com.renewsim.backend.scenario_service.web.dto;

import com.renewsim.backend.shared.domain.vo.ClimateData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ScenarioRequestDTO(
        @NotBlank(message = "Scenario name is required") String name,
        String description,
        @NotNull(message = "Technology id is required") @Positive(message = "Technology id must be positive") Long technologyId,
        @DecimalMin(value = "0.0001", message = "Default capacity must be greater than zero") double defaultCapacityKw,
        @NotNull(message = "Default investment amount is required") @Positive(message = "Default investment amount must be positive") BigDecimal defaultInvestmentAmount,
        @NotBlank(message = "Default investment currency is required") String defaultInvestmentCurrency,
        @PositiveOrZero(message = "Default tariff cannot be negative") double defaultTariff,
        @PositiveOrZero(message = "Default consumption cannot be negative") double defaultConsumption,
        @NotNull(message = "Climate profile is required") @Valid ClimateData climateProfile) {
}

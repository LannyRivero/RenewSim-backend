package com.renewsim.backend.technology_service.application.command;

import jakarta.validation.constraints.*;

public record UpdateTechnologyCommand(
        @NotNull(message = "Technology ID is required") Long id,
        @NotBlank(message = "Technology name is required") String name,
        @DecimalMin(value = "0.0") @DecimalMax(value = "1.0") double efficiency,
        @Positive double installationCost,
        @Positive double maintenanceCost,
        @Min(0) @Max(100) double environmentalImpact,
        @PositiveOrZero double co2Reduction,
        @Positive double energyProduction,
        @NotBlank String energyType) {
}

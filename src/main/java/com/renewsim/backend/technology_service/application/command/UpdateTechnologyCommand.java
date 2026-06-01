package com.renewsim.backend.technology_service.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record UpdateTechnologyCommand(
        @Schema(description = "Technology ID (injected from path variable)", example = "7") Long id,
        @NotBlank String name,
        @DecimalMin("0.0") @DecimalMax("1.0") double efficiency,
        @Positive double installationCost,
        @Positive double maintenanceCost,
        @Min(0) @Max(100) double environmentalImpact,
        @PositiveOrZero double co2Reduction,
        @Positive @DecimalMax("100") double capacityFactor,
        @NotBlank String energyType) {
    public static UpdateTechnologyCommand withId(Long id, UpdateTechnologyCommand base) {
        return new UpdateTechnologyCommand(
                id,
                base.name(),
                base.efficiency(),
                base.installationCost(),
                base.maintenanceCost(),
                base.environmentalImpact(),
                base.co2Reduction(),
                base.capacityFactor(),
                base.energyType());
    }
}

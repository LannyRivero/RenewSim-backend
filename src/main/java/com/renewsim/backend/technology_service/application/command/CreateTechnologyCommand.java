package com.renewsim.backend.technology_service.application.command;

import jakarta.validation.constraints.*;

public record CreateTechnologyCommand(
        @NotBlank(message = "Technology name is required") String name,
        @DecimalMin(value = "0.0", message = "Efficiency must be >= 0")
        @DecimalMax(value = "1.0", message = "Efficiency must be <= 1")
        double efficiency,
        @Positive(message = "Installation cost must be positive")
        double installationCost,
        @Positive(message = "Maintenance cost must be positive")
        double maintenanceCost,
        @Min(value = 0, message = "Environmental impact must be >= 0")
        @Max(value = 100, message = "Environmental impact must be <= 100")
        double environmentalImpact,
        @PositiveOrZero(message = "CO₂ reduction cannot be negative")
        double co2Reduction,
        @Positive(message = "Capacity factor must be positive")
        @DecimalMax(value = "100", message = "Capacity factor must be <= 100")
        double capacityFactor,
        @NotBlank(message = "Energy type is required")
        String energyType
) {}

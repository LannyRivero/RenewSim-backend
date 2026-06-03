package com.renewsim.backend.technology_service.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record TechnologyRequestDTO(
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
        @PositiveOrZero(message = "CO2 reduction cannot be negative")
        double co2Reduction,
        @Positive(message = "Capacity factor must be positive")
        @DecimalMax(value = "100", message = "Capacity factor must be <= 100")
        double capacityFactor,
        @NotBlank(message = "Energy type is required")
        String energyType) {
}

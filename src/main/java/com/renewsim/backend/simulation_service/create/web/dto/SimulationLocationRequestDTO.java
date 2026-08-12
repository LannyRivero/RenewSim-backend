package com.renewsim.backend.simulation_service.create.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record SimulationLocationRequestDTO(
                @NotBlank String label,
                @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") double lat,
                @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") double lon,
                @NotBlank String country,
                @NotBlank String countryCode) {
}

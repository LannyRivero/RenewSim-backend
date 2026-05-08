package com.renewsim.backend.simulation_service.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SimulationRequestDTO(
    @NotBlank String location,
    @NotBlank String energyType,
    @Positive double projectSize,
    @Positive double budget,
    @NotNull ClimateDataDTO climate,
    @Positive double energyConsumption
) {}

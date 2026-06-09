package com.renewsim.backend.simulation_service.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SimulationRequestDTO(
    @NotBlank String name,
    @NotBlank String technology,
    @Positive double installedCapacity,
    @NotNull @Valid SimulationLocationRequestDTO location
) {}

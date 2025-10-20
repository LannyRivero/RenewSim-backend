package com.renewsim.backend.simulation_service.dto;

import jakarta.validation.constraints.PositiveOrZero;

public record ClimateDataDTO(
    @PositiveOrZero double irradiance,
    @PositiveOrZero double wind,
    @PositiveOrZero double hydrology
) {}


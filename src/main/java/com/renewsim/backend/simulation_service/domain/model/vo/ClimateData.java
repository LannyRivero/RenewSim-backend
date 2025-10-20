package com.renewsim.backend.simulation_service.domain.model.vo;

public record ClimateData(
    double irradiance,
    double wind,
    double hydrology
) {}

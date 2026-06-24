package com.renewsim.backend.simulation_service.domain.model.vo;

public record ClimateData(
    double irradiance,
    double wind,
    double hydrology,
    Double temperature,
    String source,
    String period,
    String country
) {
    public ClimateData(double irradiance, double wind, double hydrology) {
        this(irradiance, wind, hydrology, null, null, null, null);
    }
}

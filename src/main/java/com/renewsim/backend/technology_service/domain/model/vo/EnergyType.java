package com.renewsim.backend.technology_service.domain.model.vo;

/**
 * Enum representing supported renewable energy sources.
 */
public enum EnergyType {
    SOLAR,
    WIND,
    @Deprecated
    EOLIC,
    HYDRO,
    GEOTHERMAL,
    BIOMASS;

    public static EnergyType fromString(String value) {
        if (value == null)
            throw new IllegalArgumentException("EnergyType cannot be null");
        return switch (value.trim().toUpperCase()) {
            case "EOLIC" -> WIND;
            default -> EnergyType.valueOf(value.trim().toUpperCase());
        };
    }
}

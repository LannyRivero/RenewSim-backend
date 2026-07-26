package com.renewsim.backend.simulation_service.domain.model.vo;

import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationLocationException;

public record SimulationLocation(
        String label,
        double lat,
        double lng,
        String country,
        CountryCode countryCode) {

    public SimulationLocation {
        if (label == null || label.isBlank()) {
            throw new InvalidSimulationLocationException("VALIDATION_ERROR: location label is required");
        }
        if (lat < -90.0 || lat > 90.0) {
            throw new InvalidSimulationLocationException("VALIDATION_ERROR: location latitude must be between -90 and 90");
        }
        if (lng < -180.0 || lng > 180.0) {
            throw new InvalidSimulationLocationException("VALIDATION_ERROR: location longitude must be between -180 and 180");
        }
        if (country == null || country.isBlank()) {
            throw new InvalidSimulationLocationException("VALIDATION_ERROR: location country is required");
        }
        if (countryCode == null) {
            throw new InvalidSimulationLocationException("VALIDATION_ERROR: location countryCode is required");
        }

        label = label.trim();
        country = country.trim();
    }

    public static SimulationLocation of(
            String label, double lat, double lng,
            String country, CountryCode countryCode) {
        return new SimulationLocation(label, lat, lng, country, countryCode);
    }
}

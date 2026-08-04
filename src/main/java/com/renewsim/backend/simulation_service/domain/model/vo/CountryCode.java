package com.renewsim.backend.simulation_service.domain.model.vo;

import com.renewsim.backend.simulation_service.domain.exception.InvalidCountryCodeException;

public record CountryCode(String value) {

    private static final String SUPPORTED_PHASE1 = "ES";

    public static CountryCode of(String value) {
        return new CountryCode(value);
    }

    public static boolean isSupported(String value) {
        return value != null && SUPPORTED_PHASE1.equalsIgnoreCase(value.trim());
    }

    public CountryCode {
        if (value == null || value.isBlank()) {
            throw new InvalidCountryCodeException("VALIDATION_ERROR: countryCode is required");
        }
        if (!isSupported(value)) {
            throw new InvalidCountryCodeException("LOCATION_NOT_FOUND: only Spain locations are supported in phase 1");
        }
        value = value.trim().toUpperCase();
    }
}

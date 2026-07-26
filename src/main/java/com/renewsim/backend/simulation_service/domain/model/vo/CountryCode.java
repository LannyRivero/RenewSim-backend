package com.renewsim.backend.simulation_service.domain.model.vo;

import com.renewsim.backend.simulation_service.domain.exception.InvalidCountryCodeException;

public record CountryCode(String value) {

    public static CountryCode of(String value) {
        return new CountryCode(value);
    }

    private static final String SUPPORTED_PHASE1 = "ES";

    public CountryCode {
        if (value == null || value.isBlank()) {
            throw new InvalidCountryCodeException("VALIDATION_ERROR: countryCode is required");
        }
        if (!SUPPORTED_PHASE1.equalsIgnoreCase(value.trim())) {
            throw new InvalidCountryCodeException("LOCATION_NOT_FOUND: only Spain locations are supported in phase 1");
        }
        value = value.trim().toUpperCase();
    }
}

package com.renewsim.backend.simulation_service.domain.model.vo;

import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationCurrencyException;

public record Currency(String value) {

    public static Currency of(String value) {
        return new Currency(value);
    }

    private static final String SUPPORTED_PHASE1 = "EUR";

    public Currency {
        if (value == null || value.isBlank()) {
            throw new InvalidSimulationCurrencyException("VALIDATION_ERROR: currency is required");
        }
        if (!SUPPORTED_PHASE1.equalsIgnoreCase(value.trim())) {
            throw new InvalidSimulationCurrencyException("VALIDATION_ERROR: currency must be EUR");
        }
        value = value.trim().toUpperCase();
    }
}

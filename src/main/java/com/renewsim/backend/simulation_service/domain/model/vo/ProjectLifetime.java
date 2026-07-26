package com.renewsim.backend.simulation_service.domain.model.vo;

import com.renewsim.backend.simulation_service.domain.exception.InvalidProjectLifetimeException;

public record ProjectLifetime(int years) {

    public static ProjectLifetime of(int years) {
        return new ProjectLifetime(years);
    }

    private static final int MIN_YEARS = 5;

    public ProjectLifetime {
        if (years < MIN_YEARS) {
            throw new InvalidProjectLifetimeException("VALIDATION_ERROR: projectLifetimeYears must be at least " + MIN_YEARS);
        }
    }
}

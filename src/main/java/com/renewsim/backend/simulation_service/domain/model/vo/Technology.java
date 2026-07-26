package com.renewsim.backend.simulation_service.domain.model.vo;

import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationTechnologyException;

public record Technology(String value) {

    public Technology {
        if (value == null || value.isBlank()) {
            throw new InvalidSimulationTechnologyException("VALIDATION_ERROR: technology is required");
        }
        value = value.trim().toLowerCase();
    }

    public static Technology of(String value) {
        return new Technology(value);
    }

    public static Technology solar() {
        return new Technology("solar");
    }
}

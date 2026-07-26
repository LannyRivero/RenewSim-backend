package com.renewsim.backend.simulation_service.domain.model;

import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationIdException;

public record SimulationId(Long value) {

    public SimulationId {
        if (value == null || value <= 0) {
            throw new InvalidSimulationIdException(value);
        }
    }

    public static SimulationId of(Long value) {
        return new SimulationId(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

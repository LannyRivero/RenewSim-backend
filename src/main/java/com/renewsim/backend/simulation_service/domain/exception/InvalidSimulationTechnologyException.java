package com.renewsim.backend.simulation_service.domain.exception;

import com.renewsim.backend.shared.exception.BadRequestException;

public class InvalidSimulationTechnologyException extends BadRequestException {

    public InvalidSimulationTechnologyException(String message) {
        super(message);
    }
}

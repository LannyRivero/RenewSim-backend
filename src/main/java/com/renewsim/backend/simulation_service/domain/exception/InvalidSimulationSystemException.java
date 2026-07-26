package com.renewsim.backend.simulation_service.domain.exception;

import com.renewsim.backend.shared.exception.BadRequestException;

public class InvalidSimulationSystemException extends BadRequestException {

    public InvalidSimulationSystemException(String message) {
        super(message);
    }
}

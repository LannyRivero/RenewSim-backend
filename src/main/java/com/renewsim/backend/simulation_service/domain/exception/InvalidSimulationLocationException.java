package com.renewsim.backend.simulation_service.domain.exception;

import com.renewsim.backend.shared.exception.BadRequestException;

public class InvalidSimulationLocationException extends BadRequestException {

    public InvalidSimulationLocationException(String message) {
        super(message);
    }
}

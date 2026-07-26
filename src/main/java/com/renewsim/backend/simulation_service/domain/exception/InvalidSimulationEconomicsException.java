package com.renewsim.backend.simulation_service.domain.exception;

import com.renewsim.backend.shared.exception.BadRequestException;

public class InvalidSimulationEconomicsException extends BadRequestException {

    public InvalidSimulationEconomicsException(String message) {
        super(message);
    }
}

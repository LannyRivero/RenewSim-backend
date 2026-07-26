package com.renewsim.backend.simulation_service.domain.exception;

import com.renewsim.backend.shared.exception.BadRequestException;

public class InvalidSimulationCurrencyException extends BadRequestException {

    public InvalidSimulationCurrencyException(String message) {
        super(message);
    }
}

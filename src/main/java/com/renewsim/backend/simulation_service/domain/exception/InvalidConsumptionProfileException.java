package com.renewsim.backend.simulation_service.domain.exception;

import com.renewsim.backend.shared.exception.BadRequestException;

public class InvalidConsumptionProfileException extends BadRequestException {

    public InvalidConsumptionProfileException(String message) {
        super(message);
    }
}

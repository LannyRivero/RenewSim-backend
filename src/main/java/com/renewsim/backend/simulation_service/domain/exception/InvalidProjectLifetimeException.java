package com.renewsim.backend.simulation_service.domain.exception;

import com.renewsim.backend.shared.exception.BadRequestException;

public class InvalidProjectLifetimeException extends BadRequestException {

    public InvalidProjectLifetimeException(String message) {
        super(message);
    }
}

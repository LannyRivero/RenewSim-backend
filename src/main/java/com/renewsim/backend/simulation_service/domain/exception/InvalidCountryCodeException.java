package com.renewsim.backend.simulation_service.domain.exception;

import com.renewsim.backend.shared.exception.BadRequestException;

public class InvalidCountryCodeException extends BadRequestException {

    public InvalidCountryCodeException(String message) {
        super(message);
    }
}

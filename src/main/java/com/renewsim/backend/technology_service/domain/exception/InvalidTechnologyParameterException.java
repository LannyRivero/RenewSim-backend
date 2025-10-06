package com.renewsim.backend.technology_service.domain.exception;

public class InvalidTechnologyParameterException extends RuntimeException {
    public InvalidTechnologyParameterException(String message) {
        super(message);
    }
}


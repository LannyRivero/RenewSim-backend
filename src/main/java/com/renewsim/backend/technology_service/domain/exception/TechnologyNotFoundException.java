package com.renewsim.backend.technology_service.domain.exception;

public class TechnologyNotFoundException extends RuntimeException {
    public TechnologyNotFoundException(Long id) {
        super("Technology with ID " + id + " not found");
    }
}


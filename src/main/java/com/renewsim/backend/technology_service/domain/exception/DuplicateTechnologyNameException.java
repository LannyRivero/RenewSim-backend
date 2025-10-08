package com.renewsim.backend.technology_service.domain.exception;

public class DuplicateTechnologyNameException extends RuntimeException {
    public DuplicateTechnologyNameException(String name) {
        super("Technology with name '" + name + "' already exists");
    }
}


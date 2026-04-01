package com.renewsim.backend.technology_service.domain.exception;

/**
 * Domain exception thrown when attempting to create a technology with a name that already exists.
 * Represents a business rule violation: technology names must be unique.
 */
public class DuplicateTechnologyNameException extends RuntimeException {
    
    public DuplicateTechnologyNameException(String name) {
        super("Technology with name '" + name + "' already exists");
    }
}

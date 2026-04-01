package com.renewsim.backend.shared.domain.exception;

public class InvalidEmailException extends IllegalArgumentException  {
    public InvalidEmailException(String message) {
        super(message);
    }
}

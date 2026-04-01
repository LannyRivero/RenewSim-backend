package com.renewsim.backend.shared.domain.exception;

public class InvalidPasswordException extends IllegalArgumentException  {
    public InvalidPasswordException(String message) {
        super(message);
    }

}
package com.renewsim.backend.shared.domain.exception;

public class InvalidLocationException extends IllegalArgumentException  {
    public InvalidLocationException(String message) {
        super(message);
    }

}

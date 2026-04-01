package com.renewsim.backend.shared.domain.vo;

import com.renewsim.backend.shared.domain.exception.InvalidPasswordException;

public record Password(String value) {
    
    public Password {
        if (value == null) {
            throw new InvalidPasswordException("Password cannot be null");
        }
        if (value.length() < 8) {
            throw new InvalidPasswordException("Password must be at least 8 characters");
        }
        if (!value.matches(".*[A-Z].*")) {
            throw new InvalidPasswordException("Password must contain at least one uppercase letter");
        }
        if (!value.matches(".*\\d.*")) {
            throw new InvalidPasswordException("Password must contain at least one digit");
        }
        if (!value.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new InvalidPasswordException("Password must contain at least one symbol");
        }
    }
}

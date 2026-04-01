package com.renewsim.backend.shared.domain.vo;

import java.util.regex.Pattern;

import com.renewsim.backend.shared.domain.exception.InvalidEmailException;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new InvalidEmailException("Email cannot be null or blank");
        }
        if (!EMAIL_PATTERN.matcher(value.trim()).matches()) {
            throw new InvalidEmailException("Invalid email format: " + value);
        }
        value = value.trim().toLowerCase();
    }
}

package com.renewsim.backend.auth_service.domain.error;

public enum AuthErrorCode {

    AUTH_INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS", "Invalid credentials"),
    AUTH_USER_DISABLED("AUTH_USER_DISABLED", "User account is disabled"),
    AUTH_USERNAME_CONFLICT("AUTH_USERNAME_CONFLICT", "Username already exists"),
    AUTH_EMAIL_CONFLICT("AUTH_EMAIL_CONFLICT", "Email already registered");

    private final String code;
    private final String defaultMessage;

    AuthErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String code() { return code; }
    public String defaultMessage() { return defaultMessage; }
}
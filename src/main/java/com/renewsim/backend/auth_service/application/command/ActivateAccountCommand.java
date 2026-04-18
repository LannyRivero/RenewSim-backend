package com.renewsim.backend.auth_service.application.command;

/**
 * Command for activating a user account via token.
 */
public record ActivateAccountCommand(String token) {}
package com.renewsim.backend.auth_service.application.command;

public record RegisterCommand(String fullName, String password, String email) {}

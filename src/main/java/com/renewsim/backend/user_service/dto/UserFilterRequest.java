package com.renewsim.backend.user_service.dto;

public record UserFilterRequest(
    String username,
    String email,
    Boolean enabled
) {}


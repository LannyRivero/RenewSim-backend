package com.renewsim.backend.auth_service.application.result;

import com.renewsim.backend.user_service.domain.model.UserStatus;

public record RegisterResult(Long id, String email, String fullName, UserStatus status, String message) {}

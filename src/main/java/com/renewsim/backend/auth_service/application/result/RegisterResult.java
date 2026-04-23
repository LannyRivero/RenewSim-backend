package com.renewsim.backend.auth_service.application.result;

import com.renewsim.backend.auth_service.domain.model.AuthUserStatus;

public record RegisterResult(Long id, String email, String fullName, AuthUserStatus status, String message) {
}

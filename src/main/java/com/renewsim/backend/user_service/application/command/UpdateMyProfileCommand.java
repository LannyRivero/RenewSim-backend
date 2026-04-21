package com.renewsim.backend.user_service.application.command;

public record UpdateMyProfileCommand(Long userId, String fullName, String phone) {
}
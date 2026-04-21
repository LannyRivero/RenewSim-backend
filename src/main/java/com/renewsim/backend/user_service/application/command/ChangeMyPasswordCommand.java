package com.renewsim.backend.user_service.application.command;

public record ChangeMyPasswordCommand(Long userId, String currentPassword, String newPassword) {
}
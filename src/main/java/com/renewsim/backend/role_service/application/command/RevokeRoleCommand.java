package com.renewsim.backend.role_service.application.command;

public record RevokeRoleCommand(Long requesterId, Long targetUserId, Long roleId) {}


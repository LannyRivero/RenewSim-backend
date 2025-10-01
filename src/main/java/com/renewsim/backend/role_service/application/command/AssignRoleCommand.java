package com.renewsim.backend.role_service.application.command;

public record AssignRoleCommand(Long requesterId, Long targetUserId, Long roleId) {}

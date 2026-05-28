package com.renewsim.backend.role_service.application.dto;

import java.util.List;

public record UserRolesUpdateRequest(List<String> roles) {
}

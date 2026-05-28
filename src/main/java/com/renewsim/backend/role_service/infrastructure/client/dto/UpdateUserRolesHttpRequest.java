package com.renewsim.backend.role_service.infrastructure.client.dto;

import java.util.List;

public record UpdateUserRolesHttpRequest(List<String> roles) {
}

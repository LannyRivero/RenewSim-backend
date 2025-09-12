package com.renewsim.backend.auth_service.web.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalUserSnapshot(
        String username,
        @JsonProperty("passwordHash") String passwordHash,
        String email,
        Set<String> roles) {
}

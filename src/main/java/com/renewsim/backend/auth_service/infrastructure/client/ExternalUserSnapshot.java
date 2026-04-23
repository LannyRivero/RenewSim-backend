package com.renewsim.backend.auth_service.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Snapshot of a user as received from the user-service")
public record ExternalUserSnapshot(
        @Schema(description = "User ID") Long id,
        String username,
        String fullName,
        @Schema(description = "Hashed password of the user") @JsonProperty("passwordHash") String passwordHash,
        @Schema(description = "Email address of the user") String email,
        @Schema(description = "Roles assigned to the user") Set<String> roles,
        String status) {}

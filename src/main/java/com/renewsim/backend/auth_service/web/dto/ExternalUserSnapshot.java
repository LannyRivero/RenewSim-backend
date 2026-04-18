package com.renewsim.backend.auth_service.web.dto;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Snapshot of a user as received from an external service")
public record ExternalUserSnapshot(
        @Schema(description = "User ID") Long id,
        String username,
        @Schema(description = "Hashed password of the user") @JsonProperty("passwordHash") String passwordHash,
        @Schema(description = "Email address of the user") String email,
        @Schema(description = "Roles assigned to the user") Set<String> roles) {
}
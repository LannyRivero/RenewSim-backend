package com.renewsim.backend.auth_service.web.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Snapshot of a user as received from an external service")
public record ExternalUserSnapshot(
                String username,

                @Schema(description = "Hashed password of the user", example = "$2a$10$GEx1so2GtWULhdDUwcsC9ujAq4Q1M2sfraQO.8VlZ8itz4inFm1LO") 
                @JsonProperty("passwordHash") 
                String passwordHash,

                @Schema(description = "Email address of the user", example = "external.admin@example.com") 
                String email,

                @Schema(description = "Roles assigned to the user", example = "[\"EXTERNAL_USER\", \"VIEWER\"]") 
                Set<String> roles) {
}

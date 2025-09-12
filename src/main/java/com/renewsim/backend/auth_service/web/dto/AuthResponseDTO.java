package com.renewsim.backend.auth_service.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authentication response containing the JWT token and user details")
public class AuthResponseDTO {

    private final String token;
    @Schema(description = "JWT access token used for authentication", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY5NDAwMDAwMCwiZXhwIjoxNjk0MDAzNjAwfQ.abc123xyz")
    private final String tokenType;

    @Schema(description = "Expiration date and time of the token (UTC, ISO-8601)", example = "2025-09-12T10:00:00Z")
    private final Instant expiresAt;

    @Schema(description = "Username of the authenticated user", example = "admin")
    private final String username;

    @Schema(description = "Roles assigned to the user", example = "[\"ADMIN\", \"USER\"]")
    private final Set<String> roles;

    @Schema(description = "Scopes granted to the user", example = "[\"user:read\", \"user:write\"]")
    private final Set<String> scopes;
}

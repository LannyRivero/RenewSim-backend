package com.renewsim.backend.auth_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

/**
 * Response DTO for successful login.
 * 
 * Note: refreshToken is NOT included here - it's sent via HttpOnly cookie.
 */
@Schema(description = "Login response with JWT access token")
public record LoginResponseDTO(

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzUxMiJ9...")
    String accessToken,

    @Schema(description = "Token type", example = "Bearer")
    String tokenType,

    @Schema(description = "Token expiration time in seconds", example = "3600")
    long expiresIn,

    @Schema(description = "User ID", example = "123")
    Long userId,

    @Schema(description = "Username", example = "john.doe")
    String username,

    @Schema(description = "User roles", example = "[\"USER\"]")
    Set<String> roles
) {}
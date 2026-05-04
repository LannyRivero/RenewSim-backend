package com.renewsim.backend.auth_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user login.
 */
@Schema(description = "Login credentials")
public record LoginRequestDTO(

                @Schema(description = "User email address", example = "user@example.com") @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,

                @Schema(description = "User password", example = "SecurePass123!") @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters") String password) {
}
package com.renewsim.backend.user_service.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record UserCreateRequest(

        @NotBlank @Pattern(regexp = "^[a-z0-9._-]{3,32}$", message = "Username must be 3-32 chars, lowercase letters, digits, . _ -") String username,

        @NotBlank @Email(message = "Email must be valid") String email,

        @NotEmpty(message = "At least one role must be provided") Set<@Pattern(regexp = "^[A-Z_]{3,32}$", message = "Role must be uppercase letters/underscores 3-32 chars") String> roles) {
}

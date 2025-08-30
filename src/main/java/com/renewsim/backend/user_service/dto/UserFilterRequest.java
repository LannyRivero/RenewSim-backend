package com.renewsim.backend.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserFilterRequest(

        @Size(min = 3, max = 64, message = "Username filter must be between 3 and 64 characters") String username,

        @Email(message = "Email must be valid") String email,

        Boolean enabled) {
}

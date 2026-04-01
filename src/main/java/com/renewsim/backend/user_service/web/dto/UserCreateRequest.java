package com.renewsim.backend.user_service.web.dto;

import jakarta.validation.constraints.*;

public record UserCreateRequest(

        @NotBlank 
        @Pattern(
                regexp = "^[a-z0-9._-]{3,32}$", 
                message = "Username must be 3-32 chars, lowercase letters, digits, . _ -"
        ) 
        String username,

        @NotBlank 
        @Email(message = "Email must be valid") 
        @Size(max = 255, message = "Email must be at most 255 characters") 
        String email,

        @NotBlank(message = "Password is required") 
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters") String password
) {}

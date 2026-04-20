package com.renewsim.backend.user_service.web.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMyProfileRequestDTO(
        @Size(max = 255, message = "Full name must be at most 255 characters")
        String fullName,

        @Pattern(regexp = "^\\+?[0-9]{1,20}$", message = "Phone must be valid international format")
        String phone) {}
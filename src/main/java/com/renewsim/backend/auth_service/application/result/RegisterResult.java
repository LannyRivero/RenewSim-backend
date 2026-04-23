package com.renewsim.backend.auth_service.application.result;

import com.renewsim.backend.user_service.domain.model.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResult {
    private Long id;
    private String email;
    private String fullName;
    private UserStatus status;
    private String message;
}
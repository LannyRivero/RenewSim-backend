package com.renewsim.backend.auth_service.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCommand {
    private String fullName;
    private String password;
    private String email;
}
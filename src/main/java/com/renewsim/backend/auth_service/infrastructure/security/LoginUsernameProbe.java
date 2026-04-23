package com.renewsim.backend.auth_service.infrastructure.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUsernameProbe {
    private String username;
    private String email;

    public String getUsername() { return username != null ? username : email; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
}

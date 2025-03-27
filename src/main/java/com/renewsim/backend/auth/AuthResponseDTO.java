package com.renewsim.backend.auth;

public class AuthResponseDTO {
    private String token;
    private String role;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token,String role) {
        this.token = token;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }
}

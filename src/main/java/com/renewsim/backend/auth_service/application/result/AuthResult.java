package com.renewsim.backend.auth_service.application.result;

import com.renewsim.backend.shared.domain.vo.RoleName;

import java.time.Instant;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResult {
    private String token;
    private String tokenType;
    private Instant expiresAt;
    private String username;
    private Set<RoleName> roles;
    private Set<String> scopes;
}
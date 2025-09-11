package com.renewsim.backend.auth_service.web.dto;

import com.renewsim.backend.role.RoleName;
import java.util.Set;

public record UserSnapshot(
        String username,
        String passwordHash,
        String email,  
        Set<RoleName> roles,
        boolean enabled) {

    /*
     * Factory method para crear un usuario activo (enabled = true).
     */

    public static UserSnapshot active(String username, String passwordHash, String email, Set<RoleName> roles) {
        return new UserSnapshot(username, passwordHash, email, roles, true);
    }

    /*
     * Factory method para crear un usuario deshabilitado (enabled = false).
     */
    public static UserSnapshot disabled(String username, String passwordHash, String email, Set<RoleName> roles) {
        return new UserSnapshot(username, passwordHash, email, roles, false);
    }
}

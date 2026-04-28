package com.renewsim.backend.auth_service.application.transformer;

import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Transformer para convertir roles de dominio a representación de seguridad.
 *
 * Extrae la lógica duplicada que existía en LoginStep2Service y RefreshTokenService.
 * Proporciona métodos estáticos para transformación de roles.
 */
public final class RoleTransformer {

    private RoleTransformer() {}

    /**
     * Transforma los roles del usuario (RoleName) a Set de Strings para AuthenticatedUser.
     * Convierte cada rol a su representacion en mayúsculas (ej: ADMIN, USER).
     *
     * @param userSnapshot el usuario con roles
     * @return Set de Strings con los nombres de roles
     */
    public static Set<String> toRoleNames(UserSnapshot userSnapshot) {
        return userSnapshot.roles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    /**
     * Transforma roles a AuthenticatedUser.
     *
     * @param username el nombre de usuario
     * @param roles el set de roles
     * @return AuthenticatedUser con los roles conversionName
     */
    public static AuthenticatedUser toAuthenticatedUser(String username, Set<String> roles) {
        return new AuthenticatedUser(username, roles, Set.of());
    }
}
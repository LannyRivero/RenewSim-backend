package com.renewsim.backend.auth_service.application.validator;

import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.shared.exception.AuthenticationException;

/**
 * Validador centralizado para cuentas de usuario.
 * Ubicación: Capa de Aplicación (sin dependencias de framework).
 */
public class UserAccountValidator {

    /**
     * Verifica si el usuario está habilitado.
     * @param user el usuario a validar
     * @return true si el usuario está habilitado, false en caso contrario
     */
    public boolean isEnabled(UserSnapshot user) {
        return user != null && user.enabled();
    }

    /**
     * Valida que el usuario esté habilitado, lanzando excepción si no lo está.
     * Adecuado para casos donde se debe revelar que la cuenta no está activa.
     * @param user el usuario a validar
     * @throws AuthenticationException si el usuario no está habilitado
     */
    public void validateEnabledOrThrow(UserSnapshot user) {
        if (!isEnabled(user)) {
            throw new AuthenticationException("Account is not active");
        }
    }
}

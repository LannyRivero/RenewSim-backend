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

    /**
     * Verifica si el email del usuario está verificado.
     * @param user el usuario a validar
     * @return true si el email está verificado, false en caso contrario
     */
    public boolean isEmailVerified(UserSnapshot user) {
        return user != null && user.enabled();
    }

    /**
     * Valida que el email esté verificado, lanzando excepción si no lo está.
     * CRÍTICO: El login debe rechazar usuarios sin email verificado.
     * @param user el usuario a validar
     * @throws AuthenticationException si el email no está verificado
     */
    public void validateEmailVerifiedOrThrow(UserSnapshot user) {
        if (!isEmailVerified(user)) {
            throw new AuthenticationException("Email not verified");
        }
    }
}

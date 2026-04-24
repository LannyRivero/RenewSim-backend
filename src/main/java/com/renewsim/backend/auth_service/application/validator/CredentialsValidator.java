package com.renewsim.backend.auth_service.application.validator;

import com.renewsim.backend.auth_service.application.port.out.PasswordEncoderPort;
import com.renewsim.backend.shared.error.ErrorMessageFactory;
import com.renewsim.backend.shared.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.renewsim.backend.auth_service.domain.error.AuthErrorCode.AUTH_INVALID_CREDENTIALS;

/**
 * Validador de credenciales de usuario.
 *
 * Ubicación: Capa de Aplicación (no Domain).
 * Responsabilidad: Orquestar validaciones estructurales y de autenticación.
 * 
 * NOTA: Aunque está en application layer, necesita @Component para inyección de
 * dependencias.
 * Recibe parámetros primitivos o objetos del dominio, no DTOs de la capa web.
 */
@Component
@RequiredArgsConstructor
public class CredentialsValidator {

    private final PasswordEncoderPort passwordEncoder;

    /**
     * Valida los campos estructurales de las credenciales (username y password).
     * Esta es una validación de formato, no de negocio profundo.
     *
     * @param username el nombre de usuario o email
     * @param password la contraseña en texto plano
     */
    public void validateCredentials(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }

    /**
     * Valida si el usuario está habilitado.
     * Lógica de negocio de autenticación.
     *
     * @param enabled true si el usuario está activo
     */
    public void validateUserEnabled(boolean enabled) {
        if (!enabled) {
            throw new IllegalStateException("User is disabled");
        }
    }

    /**
     * Valida si la contraseña en texto plano coincide con el hash almacenado.
     * Usa el PasswordEncoderPort (abstracción de la capa de aplicación).
     *
     * @param rawPassword     contraseña en texto plano
     * @param encodedPassword hash almacenado
     */
    public void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new AuthenticationException(
                    ErrorMessageFactory.build(AUTH_INVALID_CREDENTIALS));
        }
    }
}
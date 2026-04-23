package com.renewsim.backend.auth_service.infrastructure.security;

import com.renewsim.backend.auth_service.application.port.out.PasswordEncoderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura para operaciones criptográficas.
 *
 * Ubicación: Capa de Infraestructura (infrastructure/security/).
 * Implementa: PasswordEncoderPort (definido en Application Layer).
 * 
 * Nota: Es CORRECTO usar @Component y PasswordEncoder de Spring aquí,
 * porque la capa de infraestructura es el único lugar donde 
 * deben existir las dependencias técnicas/frameworks.
 */
@Component
@RequiredArgsConstructor
public class PasswordEncoderAdapter implements PasswordEncoderPort {

    private final PasswordEncoder springPasswordEncoder;

    /**
     * Valida si el password en texto plano coincide con el hash.
     * Delega la lógica al PasswordEncoder de Spring Security.
     */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return springPasswordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Genera un hash BCrypt para el password en texto plano.
     * Delega la lógica al PasswordEncoder de Spring Security.
     */
    @Override
    public String encode(String rawPassword) {
        return springPasswordEncoder.encode(rawPassword);
    }
}

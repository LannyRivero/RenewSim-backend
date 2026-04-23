package com.renewsim.backend.auth_service.application.port.out;

/**
 * Puerto de salida (Output Port) para operaciones criptográficas de contraseñas.
 * 
 * Abstrae la dependencia de Spring Security PasswordEncoder,
 * permitiendo que la capa de aplicación sea agnóstica al framework.
 * 
 * Este puerto sigue el principio de Inversión de Dependencias (DIP) de SOLID.
 */
public interface PasswordEncoderPort {

    /**
     * Verifica si una contraseña en texto plano coincide con el hash almacenado.
     *
     * @param rawPassword      la contraseña en texto plano
     * @param encodedPassword  el hash almacenado
     * @return true si coinciden, false en caso contrario
     */
    boolean matches(String rawPassword, String encodedPassword);

    /**
     * Genera un hash para la contraseña en texto plano.
     *
     * @param rawPassword la contraseña en texto plano
     * @return el hash generado
     */
    String encode(String rawPassword);
}

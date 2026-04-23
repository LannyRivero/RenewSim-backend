package com.renewsim.backend.auth_service.application.dto;

/**
 * DTO específico para credenciales de autenticación.
 * 
 * Ubicación: application/dto/ (Capa de Aplicación).
 * Propósito: Transportar únicamente los datos necesarios para validar
 * credenciales.
 * 
 * Nota: Es un DTO de aplicación, NO de web (web/dto/).
 * Al estar en application/, la capa de aplicación puede usarlo sin depender de
 * web/.
 * 
 * Seguridad: Solo contiene lo mínimo necesario para autenticación.
 * NO usa ExternalUserSnapshot directamente - eso lo hará el adaptador
 * específico.
 */
public record UserCredentials(
        Long id,
        String passwordHash, // Necesario para validar credenciales
        boolean active // Necesario para verificar si el usuario está activo
) {
}

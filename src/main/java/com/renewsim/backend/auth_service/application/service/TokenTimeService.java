package com.renewsim.backend.auth_service.application.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Servicio de aplicación para cálculos de tiempo en tokens.
 * 
 * Ubicación: Capa de Aplicación (no Domain).
 * Responsabilidad: Orquestar tiempos de expiración para tokens.
 * 
 * Nota: Es un POJO (Plain Old Java Object), sin anotaciones de Spring.
 * La capa de aplicación puede conocer detalles técnicos de tiempo,
 * pero no debe exponer frameworks como Spring en domain.
 */
public class TokenTimeService {

    private static final int EXPIRATION_MINUTES = 60;

    /**
     * Calcula el instante de expiración basado en el tiempo actual.
     * 
     * @return el instante de expiración
     */
    public Instant calculateExpiration() {
        return Instant.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES);
    }

    /**
     * Verifica si el tiempo actual ha pasado el instante de expiración.
     * 
     * @return true si ha expirado, false en caso contrario
     */
    public boolean isExpired() {
        return Instant.now().isAfter(calculateExpiration());
    }
}

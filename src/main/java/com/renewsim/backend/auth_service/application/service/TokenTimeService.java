package com.renewsim.backend.auth_service.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Servicio de aplicación para cálculos de tiempo en tokens.
 * 
 * Ubicación: Capa de Aplicación (no Domain).
 * Responsabilidad: Orquestar tiempos de expiración para tokens.
 * 
 * NOTA: Aunque es lógica simple, necesita @Component para inyección de dependencias.
 * La capa de aplicación puede conocer detalles técnicos de tiempo,
 * pero no debe exponer frameworks en el dominio.
 */
@Component
public class TokenTimeService {

    @Value("${jwt.expiration.minutes:60}")
    private int expirationMinutes;

    /**
     * Calcula el instante de expiración basado en el tiempo actual.
     * 
     * @return el instante de expiración
     */
    public Instant calculateExpiration() {
        return Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES);
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
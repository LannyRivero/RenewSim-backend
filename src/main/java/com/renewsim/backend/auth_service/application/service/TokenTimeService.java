package com.renewsim.backend.auth_service.application.service;

/**
 * Servicio de aplicación que encapsula configuración de tiempos de expiración.
 * 
 * Responsabilidad: Proveer valores de configuración para tokens JWT.
 * 
 * @since 1.0.0
 */
public class TokenTimeService {

    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;

    public TokenTimeService(long accessTokenValiditySeconds, long refreshTokenValiditySeconds) {
        if (accessTokenValiditySeconds <= 0) {
            throw new IllegalArgumentException("accessTokenValiditySeconds must be positive");
        }
        if (refreshTokenValiditySeconds <= 0) {
            throw new IllegalArgumentException("refreshTokenValiditySeconds must be positive");
        }
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    public long getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    public long getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }
}
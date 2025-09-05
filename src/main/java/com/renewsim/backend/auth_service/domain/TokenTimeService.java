package com.renewsim.backend.auth_service.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TokenTimeService {

    private static final int EXPIRATION_MINUTES = 60;

    public Instant calculateExpiration() {
        return Instant.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(calculateExpiration());
    }
}

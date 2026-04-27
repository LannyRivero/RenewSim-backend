package com.renewsim.backend.auth_service.infrastructure.config;

import com.renewsim.backend.auth_service.application.port.out.TimeProvider;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Adaptador de infraestructura que provee el Clock del sistema.
 * Puede ser reemplazado en tests con un Clock fijo.
 */
@Component
public class ClockTimeProvider implements TimeProvider {

    private final Clock clock;

    public ClockTimeProvider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Clock getClock() {
        return clock;
    }
}
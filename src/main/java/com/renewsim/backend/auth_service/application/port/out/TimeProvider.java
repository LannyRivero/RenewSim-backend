package com.renewsim.backend.auth_service.application.port.out;

import java.time.Clock;

/**
 * Puerto de salida para obtener la hora actual.
 * Abstrae la dependencia de java.time.Clock para facilitar testing.
 */
public interface TimeProvider {

    /**
     * Returns the current instant based on the default time zone.
     */
    Clock getClock();
}
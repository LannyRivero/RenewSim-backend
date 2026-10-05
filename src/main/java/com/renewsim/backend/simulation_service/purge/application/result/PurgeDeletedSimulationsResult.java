package com.renewsim.backend.simulation_service.purge.application.result;

import java.time.LocalDateTime;

public record PurgeDeletedSimulationsResult(
        int purgedCount,
        int retentionDays,
        LocalDateTime cutoff) {
}

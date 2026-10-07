package com.renewsim.backend.simulation_service.purge.application.port.out;

import java.time.LocalDateTime;

public interface PurgeDeletedSimulationRepositoryPort {

    int purgeDeletedOlderThan(LocalDateTime cutoff);
}

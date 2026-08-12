package com.renewsim.backend.simulation_service.shared.application.port.out;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;

public interface SimulationResultSnapshotReaderPort {

    SimulationDetailsResult read(String raw);
}

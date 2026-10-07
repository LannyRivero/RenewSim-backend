package com.renewsim.backend.simulation_service.purge.application.port.in;

import com.renewsim.backend.simulation_service.purge.application.command.PurgeDeletedSimulationsCommand;
import com.renewsim.backend.simulation_service.purge.application.result.PurgeDeletedSimulationsResult;

public interface PurgeDeletedSimulationsUseCase {

    PurgeDeletedSimulationsResult purgeDeletedSimulations(PurgeDeletedSimulationsCommand command);
}

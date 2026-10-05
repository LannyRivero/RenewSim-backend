package com.renewsim.backend.simulation_service.purge.application;

import com.renewsim.backend.simulation_service.purge.application.command.PurgeDeletedSimulationsCommand;
import com.renewsim.backend.simulation_service.purge.application.port.in.PurgeDeletedSimulationsUseCase;
import com.renewsim.backend.simulation_service.purge.application.port.out.PurgeDeletedSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.purge.application.result.PurgeDeletedSimulationsResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PurgeDeletedSimulationsService implements PurgeDeletedSimulationsUseCase {

    private final PurgeDeletedSimulationRepositoryPort repository;
    private final Clock clock;

    @Override
    public PurgeDeletedSimulationsResult purgeDeletedSimulations(PurgeDeletedSimulationsCommand command) {
        LocalDateTime cutoff = LocalDateTime.now(clock).minusDays(command.retentionDays());
        int purgedCount = repository.purgeDeletedOlderThan(cutoff);

        log.warn("Purged deleted simulations count={} retentionDays={} cutoff={} requestedBy={}",
                purgedCount, command.retentionDays(), cutoff, command.requestedBy());

        return new PurgeDeletedSimulationsResult(purgedCount, command.retentionDays(), cutoff);
    }
}

package com.renewsim.backend.simulation_service.purge.application;

import com.renewsim.backend.simulation_service.purge.application.command.PurgeDeletedSimulationsCommand;
import com.renewsim.backend.simulation_service.purge.application.port.out.PurgeDeletedSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.purge.application.result.PurgeDeletedSimulationsResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurgeDeletedSimulationsServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-10-01T10:00:00Z"),
            ZoneOffset.UTC);

    @Mock
    private PurgeDeletedSimulationRepositoryPort repository;

    @Test
    @DisplayName("purgeDeletedSimulations purges only records older than the retention cutoff")
    void purgeDeletedSimulationsPurgesOnlyRecordsOlderThanRetentionCutoff() {
        PurgeDeletedSimulationsService service = new PurgeDeletedSimulationsService(repository, FIXED_CLOCK);
        LocalDateTime expectedCutoff = LocalDateTime.parse("2026-09-01T10:00:00");
        when(repository.purgeDeletedOlderThan(expectedCutoff)).thenReturn(3);

        PurgeDeletedSimulationsResult result = service.purgeDeletedSimulations(
                new PurgeDeletedSimulationsCommand(30, "ops-job"));

        assertThat(result.purgedCount()).isEqualTo(3);
        assertThat(result.retentionDays()).isEqualTo(30);
        assertThat(result.cutoff()).isEqualTo(expectedCutoff);
        verify(repository).purgeDeletedOlderThan(expectedCutoff);
    }

    @Test
    @DisplayName("purgeDeletedSimulations validates the requested retention window")
    void purgeDeletedSimulationsValidatesRetentionWindow() {
        assertThatThrownBy(() -> new PurgeDeletedSimulationsCommand(0, "ops-job"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("retentionDays must be greater than zero");
    }

    @Test
    @DisplayName("purgeDeletedSimulations records the computed cutoff before calling persistence")
    void purgeDeletedSimulationsRecordsComputedCutoffBeforeCallingPersistence() {
        PurgeDeletedSimulationsService service = new PurgeDeletedSimulationsService(repository, FIXED_CLOCK);

        service.purgeDeletedSimulations(new PurgeDeletedSimulationsCommand(7, "admin-action"));

        ArgumentCaptor<LocalDateTime> cutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).purgeDeletedOlderThan(cutoff.capture());
        assertThat(cutoff.getValue()).isEqualTo(LocalDateTime.parse("2026-09-24T10:00:00"));
    }
}

package com.renewsim.backend.simulation_service.infrastructure.persistence;

import org.testcontainers.DockerClientFactory;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class MigrationItModeSelector {

    static final String PROP_PREFERRED_MODE = "migration.it.preferred-mode";
    static final String PROP_PROBE_TIMEOUT_MS = "migration.it.container-probe-timeout-ms";

    private static final String MODE_AUTO = "auto";
    private static final String MODE_CONTAINER = "container";
    private static final String MODE_JDBC_FALLBACK = "jdbc-fallback";

    private static volatile ProbeResult cachedProbeResult;

    Selection select() {
        String preferredMode = System.getProperty(PROP_PREFERRED_MODE, MODE_AUTO)
                .trim()
                .toLowerCase(Locale.ROOT);

        if (MODE_JDBC_FALLBACK.equals(preferredMode)) {
            return Selection.fallback("forced_jdbc_fallback", "Preferred mode is jdbc-fallback");
        }

        ProbeResult probeResult = getOrRunProbe();
        if (probeResult.available()) {
            return new Selection(MigrationItMode.CONTAINER, MigrationItMode.CONTAINER.defaultReasonCode(), probeResult.message());
        }

        if (MODE_CONTAINER.equals(preferredMode)) {
            return Selection.fallback("container_probe_failed", probeResult.message());
        }

        return Selection.fallback("container_unavailable", probeResult.message());
    }

    private ProbeResult getOrRunProbe() {
        ProbeResult probe = cachedProbeResult;
        if (probe != null) {
            return probe;
        }
        synchronized (MigrationItModeSelector.class) {
            probe = cachedProbeResult;
            if (probe == null) {
                probe = runProbe();
                cachedProbeResult = probe;
            }
            return probe;
        }
    }

    private ProbeResult runProbe() {
        long timeoutMillis = Long.getLong(PROP_PROBE_TIMEOUT_MS, 5000L);
        Duration timeout = Duration.ofMillis(Math.max(timeoutMillis, 250L));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> future = executor.submit(() -> DockerClientFactory.instance().isDockerAvailable());
            boolean available = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (available) {
                return new ProbeResult(true, "docker_available");
            }
            return new ProbeResult(false, "docker_not_available");
        } catch (TimeoutException e) {
            return new ProbeResult(false, "docker_probe_timeout");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ProbeResult(false, "docker_probe_interrupted");
        } catch (ExecutionException e) {
            String rootMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            return new ProbeResult(false, "docker_probe_error:" + safeMessage(rootMessage));
        } catch (Throwable t) {
            return new ProbeResult(false, "docker_probe_error:" + safeMessage(t.getMessage()));
        } finally {
            executor.shutdownNow();
        }
    }

    private String safeMessage(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replace('\n', ' ').replace('\r', ' ').trim();
    }

    record Selection(MigrationItMode mode, String reasonCode, String probeDetail) {
        static Selection fallback(String reasonCode, String detail) {
            return new Selection(MigrationItMode.JDBC_FALLBACK, reasonCode, detail);
        }
    }

    private record ProbeResult(boolean available, String message) {
    }
}

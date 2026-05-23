package com.renewsim.backend.simulation_service.infrastructure.verification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SafetyNetBaselineGateTest {

    @Test
    @DisplayName("gate should pass when every simulation row has safety-net baseline status")
    void shouldPassWhenAllRowsContainSafetyNetBaselineStatus() throws Exception {
        Path fixture = Path.of(System.getProperty("user.dir"), "sdd", "harden-simulation-verification-observability", "fixtures", "apply-progress-complete.md");

        ProcessResult result = runSafetyNetGate(fixture);

        assertThat(result.exitCode()).isZero();
        assertThat(result.stdout()).contains("Safety-net baseline completeness check PASSED");
    }

    @Test
    @DisplayName("gate should fail and report offending row ids when baseline status is missing")
    void shouldFailWhenAnyRowMissesSafetyNetBaselineStatus() throws Exception {
        Path fixture = Path.of(System.getProperty("user.dir"), "sdd", "harden-simulation-verification-observability", "fixtures", "apply-progress-missing-baseline.md");

        ProcessResult result = runSafetyNetGate(fixture);

        assertThat(result.exitCode()).isNotZero();
        assertThat(result.stderr()).contains("Missing Safety Net Baseline status for row id(s): 3.2");
    }

    private ProcessResult runSafetyNetGate(Path applyProgressPath) throws IOException, InterruptedException {
        Path script = Path.of(System.getProperty("user.dir"), "scripts", "verify", "check-safety-net-baseline.mjs");

        Process process = new ProcessBuilder(
                "node",
                script.toString(),
                "--apply-progress", applyProgressPath.toString())
                .directory(Path.of(System.getProperty("user.dir")).toFile())
                .start();

        int exitCode = process.waitFor();
        String stdout = new String(process.getInputStream().readAllBytes());
        String stderr = new String(process.getErrorStream().readAllBytes());
        return new ProcessResult(exitCode, stdout, stderr);
    }

    private record ProcessResult(int exitCode, String stdout, String stderr) {
    }
}

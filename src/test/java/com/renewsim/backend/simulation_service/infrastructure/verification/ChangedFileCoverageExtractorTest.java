package com.renewsim.backend.simulation_service.infrastructure.verification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ChangedFileCoverageExtractorTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("extractor should emit deterministic changed-file coverage JSON")
    void shouldEmitDeterministicCoverageArtifactForValidInputs() throws Exception {
        Path jacocoXml = tempDir.resolve("jacoco.xml");
        Files.writeString(jacocoXml, """
                <?xml version=\"1.0\" encoding=\"UTF-8\"?>
                <report name=\"demo\">
                  <package name=\"com/renewsim/backend/simulation_service/application/service\">
                    <sourcefile name=\"BService.java\">
                      <line nr=\"10\" mi=\"0\" ci=\"2\" mb=\"0\" cb=\"0\"/>
                      <line nr=\"11\" mi=\"2\" ci=\"0\" mb=\"0\" cb=\"0\"/>
                      <line nr=\"12\" mi=\"0\" ci=\"1\" mb=\"0\" cb=\"0\"/>
                    </sourcefile>
                    <sourcefile name=\"AService.java\">
                      <line nr=\"10\" mi=\"1\" ci=\"0\" mb=\"0\" cb=\"0\"/>
                      <line nr=\"11\" mi=\"0\" ci=\"4\" mb=\"0\" cb=\"0\"/>
                    </sourcefile>
                  </package>
                </report>
                """);

        Path changedFiles = tempDir.resolve("changed-files.txt");
        Files.writeString(changedFiles, String.join(System.lineSeparator(),
                "src/main/java/com/renewsim/backend/simulation_service/application/service/BService.java",
                "src/main/java/com/renewsim/backend/simulation_service/application/service/AService.java"));

        Path output = tempDir.resolve("verify").resolve("simulation-changed-file-coverage.json");

        ProcessResult result = runExtractor(jacocoXml, changedFiles, output);

        assertThat(result.exitCode()).isZero();
        assertThat(Files.exists(output)).isTrue();

        List<Map<String, Object>> rows = OBJECT_MAPPER.readValue(
                Files.readString(output),
                new TypeReference<List<Map<String, Object>>>() {
                });

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).get("file")).isEqualTo("src/main/java/com/renewsim/backend/simulation_service/application/service/AService.java");
        assertThat(rows.get(1).get("file")).isEqualTo("src/main/java/com/renewsim/backend/simulation_service/application/service/BService.java");

        assertThat(rows.get(0).get("coveredLines")).isEqualTo(1);
        assertThat(rows.get(0).get("missedLines")).isEqualTo(1);
        assertThat(((Number) rows.get(0).get("coverageRatio")).doubleValue()).isEqualTo(0.5d);

        assertThat(rows.get(1).get("coveredLines")).isEqualTo(2);
        assertThat(rows.get(1).get("missedLines")).isEqualTo(1);
        assertThat(((Number) rows.get(1).get("coverageRatio")).doubleValue()).isEqualTo(0.6667d);
    }

    @Test
    @DisplayName("extractor should fail when jacoco artifact is missing")
    void shouldFailWhenCoverageArtifactIsMissing() throws Exception {
        Path missingJacoco = tempDir.resolve("does-not-exist.xml");
        Path changedFiles = tempDir.resolve("changed-files.txt");
        Files.writeString(changedFiles,
                "src/main/java/com/renewsim/backend/simulation_service/application/service/AService.java");

        Path output = tempDir.resolve("verify").resolve("simulation-changed-file-coverage.json");

        ProcessResult result = runExtractor(missingJacoco, changedFiles, output);

        assertThat(result.exitCode()).isNotZero();
        assertThat(result.stderr()).contains("Missing required artifact");
        assertThat(result.stderr()).contains("does-not-exist.xml");
    }

    @Test
    @DisplayName("extractor should fail when jacoco artifact is malformed")
    void shouldFailWhenCoverageArtifactIsMalformed() throws Exception {
        Path jacocoXml = tempDir.resolve("jacoco.xml");
        Files.writeString(jacocoXml, "<report><package name=\"broken\"><sourcefile name=\"A.java\"></report>");

        Path changedFiles = tempDir.resolve("changed-files.txt");
        Files.writeString(changedFiles, "src/main/java/com/renewsim/backend/simulation_service/application/service/A.java");

        Path output = tempDir.resolve("verify").resolve("simulation-changed-file-coverage.json");

        ProcessResult result = runExtractor(jacocoXml, changedFiles, output);

        assertThat(result.exitCode()).isNotZero();
        assertThat(result.stderr()).contains("Malformed JaCoCo artifact");
    }

    private ProcessResult runExtractor(Path jacocoXml, Path changedFiles, Path output) throws IOException, InterruptedException {
        Path script = Path.of(System.getProperty("user.dir"), "scripts", "verify", "extract-simulation-changed-coverage.mjs");

        Process process = new ProcessBuilder(
                "node",
                script.toString(),
                "--jacoco", jacocoXml.toString(),
                "--changed-files", changedFiles.toString(),
                "--output", output.toString())
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

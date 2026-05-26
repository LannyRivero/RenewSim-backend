package com.renewsim.backend.simulation_service.infrastructure.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class MigrationItEvidence {

    static final String PROP_FAIL_FAST_ON_FALLBACK = "migration.it.fail-fast-on-fallback";
    static final Path EVIDENCE_PATH = Path.of("target", "verify", "migration-it-mode-evidence.json");

    private MigrationItEvidence() {
    }

    static void write(MigrationItMode mode, String fallbackReason, String isolationId) {
        String normalizedReason = safeValue(fallbackReason);
        String normalizedIsolationId = safeValue(isolationId);

        String payload = "{\n"
                + "  \"mode\": \"" + escapeJson(mode.name()) + "\",\n"
                + "  \"fallbackReason\": \"" + escapeJson(normalizedReason) + "\",\n"
                + "  \"isolationId\": \"" + escapeJson(normalizedIsolationId) + "\"\n"
                + "}\n";

        try {
            Files.createDirectories(EVIDENCE_PATH.getParent());
            Files.writeString(
                    EVIDENCE_PATH,
                    payload,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write migration IT evidence", e);
        }
    }

    static boolean isFailFastOnFallbackEnabled() {
        return Boolean.parseBoolean(System.getProperty(PROP_FAIL_FAST_ON_FALLBACK, "false"));
    }

    private static String safeValue(String value) {
        return value == null || value.isBlank() ? "n/a" : value;
    }

    private static String escapeJson(String value) {
        StringBuilder out = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}

package com.renewsim.backend.shared.dto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class OperationResponseTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("ok should expose the current correlation id in the trace field")
    void okShouldExposeCurrentCorrelationIdInTraceField() {
        MDC.put("correlationId", "corr-789");

        OperationResponse<String> response = OperationResponse.ok("data", "done");

        assertThat(response.traceId()).isEqualTo("corr-789");
    }
}

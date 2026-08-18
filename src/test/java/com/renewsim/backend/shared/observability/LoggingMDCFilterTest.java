package com.renewsim.backend.shared.observability;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingMDCFilterTest {

    private final LoggingMDCFilter filter = new LoggingMDCFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("should reuse correlation id as trace id and enrich request metadata")
    void shouldReuseCorrelationIdAsTraceIdAndEnrichRequestMetadata() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        MDC.put(CorrelationIdFilter.MDC_KEY, "corr-123");
        request.addHeader("User-Agent", "JUnit");
        request.setRemoteAddr("127.0.0.1");

        filter.doFilter(request, response, (capturedRequest, capturedResponse) -> {
            assertThat(MDC.get("traceId")).isEqualTo("corr-123");
            assertThat(MDC.get("ip")).isEqualTo("127.0.0.1");
            assertThat(MDC.get("path")).isEqualTo("/api/v1/test");
            assertThat(MDC.get("method")).isEqualTo("GET");
            assertThat(MDC.get("userAgent")).isEqualTo("JUnit");
        });

        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    @DisplayName("should expose aligned identifiers inside the filter chain")
    void shouldExposeAlignedIdentifiersInsideTheFilterChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        MDC.put(CorrelationIdFilter.MDC_KEY, "corr-456");
        request.setRemoteAddr("127.0.0.1");

        filter.doFilter(request, response, (capturedRequest, capturedResponse) -> {
            assertThat(MDC.get("traceId")).isEqualTo("corr-456");
            assertThat(MDC.get("ip")).isEqualTo("127.0.0.1");
            assertThat(MDC.get("path")).isEqualTo("/api/v1/test");
            assertThat(MDC.get("method")).isEqualTo("POST");
            assertThat(MDC.get("userAgent")).isEqualTo("N/A");
        });
    }
}

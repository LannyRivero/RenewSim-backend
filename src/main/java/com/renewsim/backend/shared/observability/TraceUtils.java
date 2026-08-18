package com.renewsim.backend.shared.observability;

import org.slf4j.MDC;

public final class TraceUtils {

    private static final String TRACE_ID = "traceId";

    private TraceUtils() {}

    public static String currentCorrelationId() {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (correlationId != null && !correlationId.isBlank()) {
            return correlationId;
        }
        String traceId = MDC.get(TRACE_ID);
        return traceId != null ? traceId : "-";
    }

    public static String currentTraceId() {
        String traceId = MDC.get(TRACE_ID);
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        return currentCorrelationId();
    }
}


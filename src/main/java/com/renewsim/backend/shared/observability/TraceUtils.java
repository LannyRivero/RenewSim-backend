package com.renewsim.backend.shared.observability;

import org.slf4j.MDC;

public final class TraceUtils {

    private TraceUtils() {}

    public static String currentTraceId() {
        String traceId = MDC.get("traceId"); 
        return traceId != null ? traceId : "-";
    }
}


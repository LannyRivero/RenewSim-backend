package com.renewsim.backend.shared.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public final class AuthAuditLogger {

    private static final Logger log = LoggerFactory.getLogger("AUTH_AUDIT");

    private AuthAuditLogger() {}

    public static void warnAuthFailure(String reason, String clientIp, String usernameOrNull) {
        log.warn("auth_failure reason={} clientIp={} username={} correlationId={}",
                safe(reason),
                safe(clientIp),
                safeUsername(usernameOrNull),
                currentCorrelationId());
    }

    public static void infoAuthSuccess(String username, String clientIp) {
        log.info("auth_success username={} clientIp={} correlationId={}",
                safe(username),
                safe(clientIp),
                currentCorrelationId());
    }

    public static void infoLogout(String username, String clientIp) {
        log.info("auth_logout username={} clientIp={} correlationId={}",
                safe(username),
                safe(clientIp),
                currentCorrelationId());
    }

    public static String currentCorrelationId() {
        String c = MDC.get(CorrelationIdFilter.MDC_KEY);
        return c == null ? "-" : c;
    }

    private static String safe(String v) {
        return v == null ? "-" : v.replaceAll("[\\r\\n\\t]", "_");
    }

    private static String safeUsername(String v) {
        if (v == null || v.isBlank()) return "-";
        return v.replaceAll("\\s+", "");
    }
}


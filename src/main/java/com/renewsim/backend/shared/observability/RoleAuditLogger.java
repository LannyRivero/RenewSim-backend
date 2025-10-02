package com.renewsim.backend.shared.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public final class RoleAuditLogger {

    private static final Logger log = LoggerFactory.getLogger("ROLE_AUDIT");

    private RoleAuditLogger() {}

    public static void roleAssigned(Long requesterId, Long targetUserId, String role) {
        log.info("role_assigned requesterId={} targetUserId={} role={} correlationId={}",
                safe(requesterId), safe(targetUserId), safe(role), currentCorrelationId());
    }

    public static void roleRevoked(Long requesterId, Long targetUserId, String role) {
        log.info("role_revoked requesterId={} targetUserId={} role={} correlationId={}",
                safe(requesterId), safe(targetUserId), safe(role), currentCorrelationId());
    }

    public static void rolesBatchUpdated(Long requesterId, Long targetUserId, Object assigned, Object revoked) {
        log.info("roles_batch_updated requesterId={} targetUserId={} assignedRoles={} revokedRoles={} correlationId={}",
                safe(requesterId), safe(targetUserId), safe(assigned), safe(revoked), currentCorrelationId());
    }

    private static String currentCorrelationId() {
        String c = MDC.get("traceId");
        return c == null ? "-" : c;
    }

    private static String safe(Object v) {
        return v == null ? "-" : v.toString().replaceAll("[\\r\\n\\t]", "_");
    }
}


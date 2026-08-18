package com.renewsim.backend.shared.dto;

import java.time.Instant;

import com.renewsim.backend.shared.observability.TraceUtils;
import com.renewsim.backend.shared.security.SecurityUtils;

public record OperationResponse<T>(
        int status,
        boolean success,
        String message,
        T data,
        Instant timestamp,
        String traceId,
        String actorUsername 
) {

    // -------------------------
    // Métodos de fábrica
    // -------------------------

    public static <T> OperationResponse<T> ok(T data, String message) {
        return new OperationResponse<>(
                200,
                true,
                message,
                data,
                Instant.now(),
                TraceUtils.currentCorrelationId(),
                SecurityUtils.currentUsername()
        );
    }

    public static <T> OperationResponse<T> created(T data, String message) {
        return new OperationResponse<>(
                201,
                true,
                message,
                data,
                Instant.now(),
                TraceUtils.currentCorrelationId(),
                SecurityUtils.currentUsername()
        );
    }

    public static OperationResponse<Void> noContent(String message) {
        return new OperationResponse<>(
                204,
                true,
                message,
                null,
                Instant.now(),
                TraceUtils.currentCorrelationId(),
                SecurityUtils.currentUsername()
        );
    }

    public static OperationResponse<Void> error(int status, String message) {
        return new OperationResponse<>(
                status,
                false,
                message,
                null,
                Instant.now(),
                TraceUtils.currentCorrelationId(),
                SecurityUtils.currentUsername()
        );
    }
}

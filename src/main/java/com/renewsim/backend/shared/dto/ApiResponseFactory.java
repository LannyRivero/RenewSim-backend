package com.renewsim.backend.shared.dto;

import com.renewsim.backend.shared.observability.TraceUtils;

public final class ApiResponseFactory {

    private ApiResponseFactory() {}

    public static <T> OperationResponse<T> ok(T data) {
        return OperationResponse.ok(
                data,
                TraceUtils.currentTraceId()
        );
    }

    public static <T> OperationResponse<T> created(T data) {
        return OperationResponse.created(
                data,
                TraceUtils.currentTraceId()
        );
    }

    public static OperationResponse<Void> noContent() {
        return OperationResponse.noContent(
                TraceUtils.currentTraceId()
        );
    }

    public static OperationResponse<Void> error(int status, String message) {
        return OperationResponse.error(
                status,
                message
        );
    }
}

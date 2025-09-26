package com.renewsim.backend.shared.dto;

import java.time.Instant;

public record ApiResponse<T>(
        int status,
        String message,
        T data,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "OK", data, Instant.now());
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "Created", data, Instant.now());
    }

    public static ApiResponse<Void> noContent() {
        return new ApiResponse<>(204, "No Content", null, Instant.now());
    }
}

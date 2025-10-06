package com.renewsim.backend.shared.dto;

/**
 * Factory class for building standardized OperationResponse instances.
 * Includes overloaded methods with optional custom messages for consistency across controllers.
 */
public final class ApiResponseFactory {

    private ApiResponseFactory() {}

    // ------------------------------------------------------------
    // ✅ 200 OK
    // ------------------------------------------------------------
    public static <T> OperationResponse<T> ok(T data) {
        return OperationResponse.ok(data, "OK");
    }

    public static <T> OperationResponse<T> ok(T data, String message) {
        return OperationResponse.ok(data, message);
    }

    // ------------------------------------------------------------
    // ✅ 201 CREATED
    // ------------------------------------------------------------
    public static <T> OperationResponse<T> created(T data) {
        return OperationResponse.created(data, "Created successfully");
    }

    public static <T> OperationResponse<T> created(T data, String message) {
        return OperationResponse.created(data, message);
    }

    // ------------------------------------------------------------
    // ✅ 204 NO CONTENT
    // ------------------------------------------------------------
    public static OperationResponse<Void> noContent() {
        return OperationResponse.noContent("No content");
    }

    public static OperationResponse<Void> noContent(String message) {
        return OperationResponse.noContent(message);
    }

    // ------------------------------------------------------------
    // ❌ ERROR
    // ------------------------------------------------------------
    public static OperationResponse<Void> error(int status, String message) {
        return OperationResponse.error(status, message);
    }
}

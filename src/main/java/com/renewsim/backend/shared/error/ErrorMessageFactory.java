package com.renewsim.backend.shared.error;

import com.renewsim.backend.auth_service.domain.error.AuthErrorCode;

public final class ErrorMessageFactory {

    private ErrorMessageFactory() {}

    public static String build(AuthErrorCode errorCode) {
        return errorCode.code() + ": " + errorCode.defaultMessage();
    }
}

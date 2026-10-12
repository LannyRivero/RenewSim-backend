package com.renewsim.backend.auth_service.application.error;

import com.renewsim.backend.auth_service.domain.error.AuthErrorCode;

public final class AuthErrorMessageFactory {

    private AuthErrorMessageFactory() {}

    public static String build(AuthErrorCode errorCode) {
        return errorCode.code() + ": " + errorCode.defaultMessage();
    }
}

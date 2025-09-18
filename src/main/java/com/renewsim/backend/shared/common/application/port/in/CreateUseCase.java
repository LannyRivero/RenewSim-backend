package com.renewsim.backend.shared.common.application.port.in;

public interface CreateUseCase<T> {
    T create(T request);
}

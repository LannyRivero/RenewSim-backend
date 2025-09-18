package com.renewsim.backend.shared.common.application.port.in;

public interface UpdateUseCase<T> {
    T update(T request);
}

package com.renewsim.backend.user_service.application.port.in.common;


public interface GetByIdUseCase<T, ID> {
    T getById(ID id);
}

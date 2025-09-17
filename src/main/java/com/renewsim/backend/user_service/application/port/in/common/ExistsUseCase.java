package com.renewsim.backend.user_service.application.port.in.common;

public interface ExistsUseCase<ID> {
    boolean exists(ID id);
}

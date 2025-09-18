package com.renewsim.backend.shared.common.application.port.in;

public interface DeleteUseCase<ID> {
    void delete(ID id);
}

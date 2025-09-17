package com.renewsim.backend.user_service.application.port.in.common;

import java.util.List;

public interface ListUseCase<T> {
    List<T> listAll();
}

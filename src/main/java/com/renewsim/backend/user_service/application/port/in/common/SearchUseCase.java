package com.renewsim.backend.user_service.application.port.in.common;

import java.util.List;

public interface SearchUseCase<T, C> {
    List<T> search(C criteria);
}
package com.renewsim.backend.user_service.application.port.out;

import com.renewsim.backend.user_service.domain.model.User;

public interface SaveUserPort {
    User saveUser(User user);
}


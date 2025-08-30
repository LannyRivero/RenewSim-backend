package com.renewsim.backend.user_service.application.port.out;

import com.renewsim.backend.user_service.domain.model.User;
import org.springframework.data.domain.Page;

public interface SearchUserPort {
    Page<User> search(String username, String email, Boolean enabled, int page, int size);
}

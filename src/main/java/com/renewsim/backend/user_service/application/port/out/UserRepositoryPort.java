package com.renewsim.backend.user_service.application.port.out;

import java.util.List;
import java.util.Optional;

import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserSearchCriteria;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    List<User> findAll();

    List<User> search(UserSearchCriteria criteria);

    void deleteById(Long id);
}

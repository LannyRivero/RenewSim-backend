package com.renewsim.backend.user_service.application.port.out;

import java.util.List;
import java.util.Optional;

import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound port for accessing and persisting users.
 *
 * Defines all persistence operations related to the User aggregate.
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    List<User> findAll();

    Page<User> search(UserFilterRequest filter, Pageable pageable);

    void deleteById(Long id);
}


package com.renewsim.backend.user_service.infrastructure.persistence.adapter;

import com.renewsim.backend.shared.exception.UserAlreadyExistsException;
import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.port.out.RoleCatalogPort;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserFilterRequest;
import com.renewsim.backend.user_service.infrastructure.mapper.UserServiceMapper;
import com.renewsim.backend.user_service.infrastructure.persistence.entity.UserEntity;
import com.renewsim.backend.user_service.infrastructure.persistence.repo.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository repo;
    private final UserServiceMapper mapper;
    private final RoleCatalogPort roleCatalogPort;

    @Override
    public Optional<User> findById(Long id) {
        return repo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repo.findByUsernameIgnoreCase(username).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmailIgnoreCase(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return repo.existsById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repo.existsByUsernameIgnoreCase(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repo.existsByEmailIgnoreCase(email);
    }

    @Override
    public List<User> findAll() {
        return repo.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Page<User> search(UserFilterRequest filter, Pageable pageable) {
        var p = repo.search(filter.username(), filter.email(), filter.enabled(), pageable);
        return p.map(mapper::toDomain);
    }

    @Override
@Transactional
public User save(User user) {
    try {
        UserEntity entity = mapper.toEntity(user);

        // Resolver roles usando el catálogo antes de guardar
        if (user.roles() != null && !user.roles().isEmpty()) {
            Set<String> validatedRoles = user.roles().stream()
                    .map(roleName -> roleCatalogPort.findByName(roleName.name())
                            .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName))
                            .name() // guardamos solo el nombre
                    )
                    .collect(Collectors.toSet());
            entity.setRoles(validatedRoles);
        }

        UserEntity saved = repo.save(entity);
        return mapper.toDomain(saved);

    } catch (DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
        log.warn("Integrity violation while saving user {}: {}", user.username(), message);

        if (message.contains("uk_users_username") || message.contains("uk_users_email")) {
            throw new UserAlreadyExistsException(
                    "User with username '" + user.username() + "' or email '" + user.email() + "' already exists");
        }
        throw ex;
    }
}

    @Override
    public void deleteById(Long id) {
        if (!repo.existsById(id)) {
            throw new UserNotFoundException("User with id=" + id + " not found");
        }
        repo.deleteById(id);
    }
}

package com.renewsim.backend.user_service.infraestructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.shared.exception.UserAlreadyExistsException;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserFilterRequest;
import com.renewsim.backend.user_service.infraestructure.mapper.UserMapper;
import com.renewsim.backend.user_service.infraestructure.persistence.entity.UserEntity;
import com.renewsim.backend.user_service.infraestructure.persistence.repo.UserJpaRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository repo;

    @Override
    public Optional<User> findById(Long id) {
        return repo.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repo.findByUsernameIgnoreCase(username).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmailIgnoreCase(email).map(UserMapper::toDomain);
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
        return repo.findAll().stream().map(UserMapper::toDomain).toList();
    }

    @Override
    public Page<User> search(UserFilterRequest filter, Pageable pageable) {
        var p = repo.search(filter.username(), filter.email(), filter.enabled(), pageable);
        return p.map(UserMapper::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        try {
            UserEntity entity = UserMapper.toEntity(user);
            UserEntity saved = repo.save(entity);
            return UserMapper.toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException(
                "User with username '" + user.username() + "' or email '" + user.email() + "' already exists"
            );
        }
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}


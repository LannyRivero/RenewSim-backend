package com.renewsim.backend.user_service.infraestructure.persistence.adapter;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.user_service.application.port.out.ExistsUserPort;
import com.renewsim.backend.user_service.application.port.out.LoadUserPort;
import com.renewsim.backend.user_service.application.port.out.SaveUserPort;
import com.renewsim.backend.user_service.application.port.out.SearchUserPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.infraestructure.mapper.UserMapper;
import com.renewsim.backend.user_service.infraestructure.persistence.entity.UserEntity;
import com.renewsim.backend.user_service.infraestructure.persistence.repo.UserJpaRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements LoadUserPort, SaveUserPort, ExistsUserPort, SearchUserPort {

    private final UserJpaRepository repo;

    @Override
    public Optional<User> loadUserById(Long id) {
        return repo.findById(id).map(UserMapper::toDomain);
    }

    public Optional<User> loadUserByUsername(String username) {
        return repo.findByUsernameIgnoreCase(username).map(UserMapper::toDomain);
    }

    public Optional<User> loadUserByEmail(String email) {
        return repo.findByEmailIgnoreCase(email).map(UserMapper::toDomain);
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        UserEntity saved = repo.save(entity);
        return UserMapper.toDomain(saved);
    }

    @Override
    public boolean existsByUsernameOrEmail(String username, String email) {
        return repo.existsByUsernameIgnoreCaseOrEmailIgnoreCase(username, email);
    }

    public Page<User> search(String username, String email, Boolean enabled, int page, int size) {
        var p = repo.search(username, email, enabled, PageRequest.of(page, size));
        return p.map(UserMapper::toDomain);
    }
}


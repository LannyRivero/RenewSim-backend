package com.renewsim.backend.user_service.infraestructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.user_service.application.port.out.ExistsUserPort;
import com.renewsim.backend.user_service.application.port.out.LoadUserPort;
import com.renewsim.backend.user_service.application.port.out.SaveUserPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.infraestructure.persistence.entity.UserEntity;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements LoadUserPort, SaveUserPort, ExistsUserPort {

    private final com.renewsim.backend.user_service.infraestructure.persistence.repo.UserJpaRepository repo;

    @Override
    public Optional<User> loadById(long id) {
        return repo.findById(id).map(UserMapper::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        UserEntity saved = repo.save(entity);
        return UserMapper.toDomain(saved);
    }

    @Override
    public boolean existsByUsernameOrEmail(String username, String email) {
        return repo.existsByUsernameIgnoreCaseOrEmailIgnoreCase(username, email);
    }

    public org.springframework.data.domain.Page<User> search(String username, String email, Boolean enabled, int page, int size) {
        var p = repo.search(username, email, enabled, PageRequest.of(page, size));
        return p.map(UserMapper::toDomain);
    }
}

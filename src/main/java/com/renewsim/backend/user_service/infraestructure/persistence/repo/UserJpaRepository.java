package com.renewsim.backend.user_service.infraestructure.persistence.repo;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.renewsim.backend.user_service.infraestructure.persistence.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    // Checks
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    // Lookups
    Optional<UserEntity> findByUsernameIgnoreCase(String username);
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    // Search with optional filters
    @Query("""
           SELECT u FROM UserEntity u
           WHERE (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
             AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
             AND (:enabled IS NULL OR u.enabled = :enabled)
           """)
    Page<UserEntity> search(
            @Param("username") String username,
            @Param("email") String email,
            @Param("enabled") Boolean enabled,
            Pageable pageable);
}


package com.renewsim.backend.user_service.infraestructure.persistence.repo;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.renewsim.backend.user_service.infraestructure.persistence.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    @Query("""
                select u from UserEntity u
                where (:username is null or lower(u.username) like lower(concat('%', :username, '%')))
                  and (:email is null or lower(u.email) like lower(concat('%', :email, '%')))
                  and (:enabled is null or u.enabled = :enabled)
            """)
    Page<UserEntity> search(
            @Param("username") String username,
            @Param("email") String email,
            @Param("enabled") Boolean enabled,
            Pageable pageable);
}

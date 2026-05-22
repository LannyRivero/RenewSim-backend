package com.renewsim.backend.user_service.infrastructure.persistence.repo;

import com.renewsim.backend.user_service.infrastructure.persistence.entity.UserEntity;
import com.renewsim.backend.shared.domain.vo.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

  boolean existsByUsernameIgnoreCase(String username);

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

  Optional<UserEntity> findByUsernameIgnoreCase(String username);

  Optional<UserEntity> findByEmailIgnoreCase(String email);

  @Query("SELECT COUNT(DISTINCT u.id) FROM UserEntity u JOIN u.roles r WHERE r.name = :roleName")
  long countByRole(@Param("roleName") RoleName roleName);

  interface UserSummary {
    Long getId();

    String getUsername();

    String getEmail();
  }

  @Query("SELECT u.id as id, u.username as username, u.email as email FROM UserEntity u")
  List<UserSummary> findAllSummaries();

  @Query("""
      SELECT u FROM UserEntity u
      WHERE (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
        AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
        AND (:enabled IS NULL
             OR (:enabled = true AND u.status = 'ACTIVE')
             OR (:enabled = false AND u.status <> 'ACTIVE'))
      """)
  Page<UserEntity> search(
      @Param("username") String username,
      @Param("email") String email,
      @Param("enabled") Boolean enabled,
      Pageable pageable);

  @Query("""
      SELECT u.id as id, u.username as username, u.email as email
      FROM UserEntity u
      WHERE (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
        AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
        AND (:enabled IS NULL
             OR (:enabled = true AND u.status = 'ACTIVE')
             OR (:enabled = false AND u.status <> 'ACTIVE'))
      """)
  Page<UserSummary> searchSummaries(
      @Param("username") String username,
      @Param("email") String email,
      @Param("enabled") Boolean enabled,
      Pageable pageable);
}

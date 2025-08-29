package com.renewsim.backend.user_service.infraestructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
    }
)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=64)
    private String username;

    @Column(nullable=false, length=128)
    private String email;

    @Column(nullable=false)
    private boolean enabled;

    @Column(nullable=false, updatable=false)
    private Instant createdAt;

    @Column(nullable=false)
    private Instant updatedAt;

    @Column(nullable=false, length=512)
    private String rolesCsv;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
        enabled = true;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}


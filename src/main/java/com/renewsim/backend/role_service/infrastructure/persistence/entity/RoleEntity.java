package com.renewsim.backend.role_service.infrastructure.persistence.entity;

import com.renewsim.backend.shared.domain.vo.RoleName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name", unique = true)
})
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, length = 50)
    private RoleName name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RoleEntity() {
    }

    public RoleEntity(RoleName name) {
        this.name = Objects.requireNonNull(name, "RoleName cannot be null");
    }

    public RoleEntity(Long id, RoleName name, String description, LocalDateTime createdAt) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "RoleName cannot be null");
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = Objects.requireNonNull(name, "RoleName cannot be null");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // equals & hashCode basados en name para evitar duplicados
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RoleEntity that))
            return false;
        return Objects.equals(id, that.id) && name == that.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "RoleEntity{id=" + id + ", name=" + name + ", description='" + description + "', createdAt=" + createdAt + '}';
    }
}

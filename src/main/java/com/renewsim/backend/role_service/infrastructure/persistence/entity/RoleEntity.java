package com.renewsim.backend.role_service.infrastructure.persistence.entity;

import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.user_service.infraestructure.persistence.entity.UserEntity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
    name = "roles",
    indexes = {
        @Index(name = "idx_role_name", columnList = "name", unique = true)
    })
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private RoleName name;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<UserEntity> users = new HashSet<>();

    public RoleEntity() {}

    public RoleEntity(RoleName name) {
        this.name = Objects.requireNonNull(name, "RoleName cannot be null");
    }

    public RoleEntity(Long id, RoleName name) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "RoleName cannot be null");

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

    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }

    // equals & hashCode basados en name para evitar duplicados
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleEntity that)) return false;
        return name == that.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "RoleEntity{name=" + name + '}';
    }
}


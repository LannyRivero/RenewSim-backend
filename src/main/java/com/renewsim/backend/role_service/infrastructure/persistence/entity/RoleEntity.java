package com.renewsim.backend.role_service.infrastructure.persistence.entity;

import java.util.Objects;

import com.renewsim.backend.role_service.domain.model.RoleName;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name", unique = true)
})
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private RoleName name;

   

    public RoleEntity() {
    }

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
        return "RoleEntity{name=" + name + '}';
    }
}

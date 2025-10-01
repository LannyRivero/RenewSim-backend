package com.renewsim.backend.role_service.infrastructure.persistence.adapter;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.ActiveProfiles;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.infrastructure.persistence.repo.RoleJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
class RolePersistenceAdapterCacheTest {

    @SpyBean
    private RoleJpaRepository roleJpaRepository;

    @Autowired
    private RolePersistenceAdapter rolePersistenceAdapter;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("renewsim_roles").clear();
    }

    @Test
    @DisplayName("findById → segunda llamada debe usar caché")
    void findById_shouldUseCacheOnSecondCall() {
        // Primera llamada - debe ir a BD
        rolePersistenceAdapter.findById(1L);
        verify(roleJpaRepository, times(1)).findById(1L);

        // Segunda llamada - debe usar caché
        rolePersistenceAdapter.findById(1L);
        verify(roleJpaRepository, times(1)).findById(1L);

        // Verificar métricas
        Cache<Object, Object> nativeCache = getCaffeineCache().getNativeCache();
        assertThat(nativeCache.stats().hitCount()).isEqualTo(1);
        assertThat(nativeCache.stats().missCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("save → debe invalidar todo el caché")
    void save_shouldInvalidateCache() {
        // Llenar caché
        rolePersistenceAdapter.findById(1L);
        rolePersistenceAdapter.findAll();

        // Guardar nuevo rol
        Role newRole = new Role(RoleName.USER);
        rolePersistenceAdapter.save(newRole);

        // Verificar que se vuelve a BD
        rolePersistenceAdapter.findById(1L);
        rolePersistenceAdapter.findAll();
        
        verify(roleJpaRepository, times(2)).findById(1L);
        verify(roleJpaRepository, times(2)).findAll();

        // Verificar métricas después de invalidación
        Cache<Object, Object> nativeCache = getCaffeineCache().getNativeCache();
        assertThat(nativeCache.stats().evictionCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("findByName → segunda llamada debe usar caché")
    void findByName_shouldUseCacheOnSecondCall() {
        // Primera llamada - debe ir a BD
        rolePersistenceAdapter.findByName(RoleName.ADMIN);
        verify(roleJpaRepository, times(1)).findByName(RoleName.ADMIN);

        // Segunda llamada - debe usar caché
        rolePersistenceAdapter.findByName(RoleName.ADMIN);
        verify(roleJpaRepository, times(1)).findByName(RoleName.ADMIN);

        // Verificar métricas
        Cache<Object, Object> nativeCache = getCaffeineCache().getNativeCache();
        assertThat(nativeCache.stats().hitCount()).isEqualTo(1);
        assertThat(nativeCache.stats().missCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("deleteById → debe invalidar caché")
    void deleteById_shouldInvalidateCache() {
        // Llenar caché
        rolePersistenceAdapter.findById(1L);
        Cache<Object, Object> nativeCache = getCaffeineCache().getNativeCache();
        assertThat(nativeCache.stats().missCount()).isEqualTo(1);

        // Eliminar rol
        roleJpaRepository.deleteById(1L);

        // Verificar que se vuelve a BD
        rolePersistenceAdapter.findById(1L);
        verify(roleJpaRepository, times(2)).findById(1L);
        assertThat(nativeCache.stats().evictionCount()).isGreaterThan(0);
    }

    private CaffeineCache getCaffeineCache() {
        return (CaffeineCache) cacheManager.getCache("renewsim_roles");
    }
}
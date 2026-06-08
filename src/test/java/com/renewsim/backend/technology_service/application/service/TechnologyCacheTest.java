package com.renewsim.backend.technology_service.application.service;

import com.renewsim.backend.shared.config.CacheConfig;
import com.renewsim.backend.technology_service.application.port.in.GetTechnologyUseCase;
import com.renewsim.backend.technology_service.application.result.TechnologyResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {TechnologyApplicationService.class, CacheConfig.class})
@ActiveProfiles("test")
class TechnologyCacheTest {

    @MockitoBean
    private TechnologyCommandService commandService;

    @Autowired
    private GetTechnologyUseCase applicationService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        var cache = cacheManager.getCache("technologies");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("getTechnologies should use cache on second identical call")
    void getTechnologiesShouldUseCacheOnSecondIdenticalCall() {
        var dtoList = List.of(
                new TechnologyResponseDTO(1L, "Solar", "SOLAR", 0.85, 1200, 25, 100,
                        null, true, null, null, 10, 250, 18));
        var page = new PageImpl<>(dtoList, PageRequest.of(0, 20), dtoList.size());

        when(commandService.handleGetAll(0, 20, null, null, null, "asc")).thenReturn(page);

        var first = applicationService.getTechnologies(0, 20, null, null, null, "asc");
        var second = applicationService.getTechnologies(0, 20, null, null, null, "asc");

        assertThat(first.getContent()).hasSize(1);
        assertThat(second.getContent()).hasSize(1);
        verify(commandService, times(1)).handleGetAll(0, 20, null, null, null, "asc");
    }
}

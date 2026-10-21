package com.renewsim.backend.simulation_service.history.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.history.application.query.SimulationHistoryPageRequest;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationRecordEntityMapper;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;
import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Simulation history persistence queries")
class SimulationHistoryQueryAdapterTest {

    @Mock
    private JpaSimulationRepository repository;

    @Mock
    private SimulationRecordEntityMapper entityMapper;

    private SimulationHistoryQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SimulationHistoryQueryAdapter(repository, entityMapper);
    }

    @Nested
    @DisplayName("history page")
    class HistoryPage {

        @Test
        @DisplayName("maps results and preserves pagination metadata")
        void mapsResultsAndPreservesPaginationMetadata() {
            var entity = new SimulationEntity();
            var readModel = new SimulationReadModel(
                    21L, "Solar - Sevilla", "solar", "COMPLETED", "Sevilla, ES",
                    1000.0, 200.0, 300.0, 12.0, "recommended", 500.0, "{}", null);
            var request = new SimulationHistoryPageRequest(
                    1, 10, SimulationHistoryPageRequest.SortField.NAME, SimulationHistoryPageRequest.SortDirection.ASC);
            var pageable = PageRequest.of(1, 10,
                    Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.DESC, "id")));
            when(repository.searchHistory("alice", null, "sevilla", pageable))
                    .thenReturn(new PageImpl<>(List.of(entity), pageable, 25));
            when(entityMapper.toReadModel(entity)).thenReturn(readModel);

            var page = adapter.searchByCreatedBy("alice", null, "sevilla", request);

            assertThat(page.content()).containsExactly(readModel);
            assertThat(page.totalElements()).isEqualTo(25);
            assertThat(page.number()).isEqualTo(1);
            assertThat(page.size()).isEqualTo(10);
            assertThat(page.totalPages()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("location suggestions")
    class LocationSuggestions {

        @Test
        @DisplayName("delegates lookup with the requested result limit")
        void delegatesLookupWithRequestedResultLimit() {
            when(repository.findLocationSuggestionsByCreatedBy("alice", "sevilla", PageRequest.of(0, 5)))
                    .thenReturn(List.of("Sevilla, ES"));

            var suggestions = adapter.findLocationSuggestionsByCreatedBy("alice", "sevilla", 5);

            assertThat(suggestions).containsExactly("Sevilla, ES");
            ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
            verify(repository).findLocationSuggestionsByCreatedBy(
                    org.mockito.ArgumentMatchers.eq("alice"), org.mockito.ArgumentMatchers.eq("sevilla"),
                    pageable.capture());
            assertThat(pageable.getValue().getPageNumber()).isZero();
            assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
        }
    }
}

package com.renewsim.backend.user_service.infrastructure.persistence.adapter;

import com.renewsim.backend.role_service.web.dto.RoleDTO;
import com.renewsim.backend.shared.dto.OperationResponse;
import com.renewsim.backend.user_service.infrastructure.client.RoleServiceClient;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleCatalogFeignAdapterTest {

    @Mock
    private RoleServiceClient roleServiceClient;

    private RoleCatalogFeignAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RoleCatalogFeignAdapter(roleServiceClient);
    }

    @Test
    @DisplayName("findById should map role response to snapshot")
    void findById_mapsRoleResponse() {
        when(roleServiceClient.findById(2L))
                .thenReturn(OperationResponse.ok(new RoleDTO(2L, "ANALYST", "Analyst", LocalDateTime.now()),
                        "Role retrieved successfully"));

        var result = adapter.findById(2L);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().id()).isEqualTo(2L);
        assertThat(result.orElseThrow().name()).isEqualTo("ANALYST");
    }

    @Test
    @DisplayName("findById should return empty when role service returns 404")
    void findById_returnsEmptyOnNotFound() {
        when(roleServiceClient.findById(99L)).thenThrow(notFound());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    private static FeignException notFound() {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/v1/roles/99",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null);
        Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .headers(Map.of())
                .build();
        return FeignException.errorStatus("RoleServiceClient#findById(Long)", response);
    }
}

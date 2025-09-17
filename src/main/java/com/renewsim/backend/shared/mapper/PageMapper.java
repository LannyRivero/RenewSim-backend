package com.renewsim.backend.shared.mapper;

import com.renewsim.backend.user_service.dto.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Utility class for converting Spring Data {@link Page} objects
 * into custom {@link PageResponse} DTOs.
 */
public class PageMapper {

    private PageMapper() {
        // Utility class, prevent instantiation
    }

    /**
     * Maps a {@link Page} of entities into a {@link PageResponse} of DTOs.
     *
     * @param <T>       the type of entities
     * @param <R>       the type of DTOs
     * @param page      the Spring Data page
     * @param converter function to map each entity to a DTO
     * @return a {@link PageResponse} containing the converted content and pagination metadata
     */
    public static <T, R> PageResponse<R> toPageResponse(Page<T> page, Function<T, R> converter) {
        List<R> content = page.getContent().stream()
                .map(converter)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}

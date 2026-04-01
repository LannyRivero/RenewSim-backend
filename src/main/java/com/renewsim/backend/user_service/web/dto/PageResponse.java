package com.renewsim.backend.user_service.web.dto;

import java.util.List;

/**
 * Generic pagination response wrapper.
 *
 * @param <T> the type of elements in the page
 */
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last
) {}



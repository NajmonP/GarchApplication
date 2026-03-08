package com.example.garchapplication.model.dto.api;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO wrapper for Spring Data {@link Page} results.
 *
 * <p>Serializing {@code PageImpl} directly is discouraged because its JSON structure
 * is not guaranteed to remain stable across Spring versions.
 * This wrapper provides a predictable and stable API response format
 * for paginated endpoints.</p>
 *
 * @param content list of items on the current page
 * @param page index of the current page
 * @param size number of elements per page
 * @param totalElements total number of elements across all pages
 * @param totalPages total number of available pages
 * @param <T> type of the content elements
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> responseFromPage(Page<T> p) {
        return new PageResponse<>(
                p.getContent(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages()
        );
    }
}

package com.demo.employees.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic wrapper for paginated query results, including page metadata.
 *
 * @param <T> the type of elements in the page content
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResp<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}

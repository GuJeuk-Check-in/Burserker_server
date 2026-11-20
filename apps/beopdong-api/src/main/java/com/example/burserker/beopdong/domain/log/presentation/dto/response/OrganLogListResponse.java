package com.example.burserker.beopdong.domain.log.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;

public record OrganLogListResponse(
        List<LogPreview> content,
        PageableResponse pageable,
        long totalElements,
        int totalPages,
        boolean last
) {
    public record LogPreview(Long id, String name, LocalDate visitDate, List<Long> purpose) {
    }

    public record PageableResponse(int pageNumber, int pageSize) {
    }
}

package com.example.burserker.beopdong.domain.log.presentation.dto.response;

import java.util.List;

public record LogListResponse(
        List<LogPreview> content,
        PageableResponse pageable
) {
    public record LogPreview(Long id, String name, List<Long> purpose, long organRemainTime) {
    }

    public record PageableResponse(int pageNumber, int pageSize) {
    }
}

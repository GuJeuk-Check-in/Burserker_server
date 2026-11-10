package com.example.burserker.beopdong.domain.organ.presentation.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record CompletedUsagePageResponse(
        List<CompletedUsageResponse> content,
        PageableResponse pageable,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static CompletedUsagePageResponse from(Page<CompletedUsageResponse> page) {
        return new CompletedUsagePageResponse(
                page.getContent(),
                new PageableResponse(page.getNumber(), page.getSize()),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public record PageableResponse(
            int pageNumber,
            int pageSize
    ) {
    }
}

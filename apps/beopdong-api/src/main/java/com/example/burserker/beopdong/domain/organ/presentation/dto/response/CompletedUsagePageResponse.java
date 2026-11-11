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

    /**
     * 완료 목록과 페이지 번호·전체 건수·마지막 페이지 여부를 기존 명세 형식으로 반환한다.
     */
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

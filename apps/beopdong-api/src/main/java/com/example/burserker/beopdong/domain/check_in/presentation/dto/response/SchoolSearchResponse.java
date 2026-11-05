package com.example.burserker.beopdong.domain.check_in.presentation.dto.response;

import java.util.List;

public record SchoolSearchResponse(
        List<SchoolResponse> schools,
        int page,
        boolean hasNext
) {
}
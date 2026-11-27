package com.example.burserker.gujeuk.domain.purpose.presentation.dto.response;

import java.util.List;

public record PurposeWithSubListResponse(
        List<PurposeWithSubResponse> purposes
) {
}

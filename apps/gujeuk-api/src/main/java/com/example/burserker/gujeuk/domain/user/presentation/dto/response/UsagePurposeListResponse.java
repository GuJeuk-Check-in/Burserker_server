package com.example.burserker.gujeuk.domain.user.presentation.dto.response;

import java.util.List;

public record UsagePurposeListResponse(
    List<UsagePurposeResponse> purposes
) {
}

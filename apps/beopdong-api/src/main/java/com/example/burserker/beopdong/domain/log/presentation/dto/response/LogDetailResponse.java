package com.example.burserker.beopdong.domain.log.presentation.dto.response;

import java.util.List;

public record LogDetailResponse(
        String name,
        List<PurposeResponse> purpose,
        long organRemainTime
) {
    public record PurposeResponse(String purposeForm, boolean purposeSelect, String purposeState, long purposeRemainTime) {
    }
}

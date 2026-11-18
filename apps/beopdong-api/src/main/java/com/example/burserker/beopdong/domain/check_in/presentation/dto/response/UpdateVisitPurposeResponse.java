package com.example.burserker.beopdong.domain.check_in.presentation.dto.response;

import com.example.burserker.beopdong.domain.log.presentation.dto.response.LogDetailResponse;

import java.util.List;

public record UpdateVisitPurposeResponse(List<LogDetailResponse.PurposeResponse> purpose) {
}

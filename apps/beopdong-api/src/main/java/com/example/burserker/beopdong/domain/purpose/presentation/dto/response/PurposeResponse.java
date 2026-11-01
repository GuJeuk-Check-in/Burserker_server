package com.example.burserker.beopdong.domain.purpose.presentation.dto.response;

import com.example.burserker.beopdong.domain.purpose.domain.Purpose;
import lombok.Builder;

@Builder
public record PurposeResponse(
        Long id,
        String purposeName
) {
    public static PurposeResponse from(Purpose purpose) {
        return PurposeResponse.builder()
                .id(purpose.getId())
                .purposeName(purpose.getPurposeName())
                .build();
    }
}

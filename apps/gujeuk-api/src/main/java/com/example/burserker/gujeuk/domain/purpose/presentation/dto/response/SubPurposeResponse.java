package com.example.burserker.gujeuk.domain.purpose.presentation.dto.response;

import com.example.burserker.gujeuk.domain.purpose.domain.SubPurpose;
import lombok.Builder;

@Builder
public record SubPurposeResponse(
        Long subPurposeId,
        String subPurpose,
        boolean subPurposeReservation
) {
    public static SubPurposeResponse from(SubPurpose subPurpose) {
        return SubPurposeResponse.builder()
                .subPurposeId(subPurpose.getId())
                .subPurpose(subPurpose.getSubPurposeName())
                .subPurposeReservation(subPurpose.isReservable())
                .build();
    }
}

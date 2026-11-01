package com.example.burserker.gujeuk.domain.purpose.presentation.dto.response;

import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import lombok.Builder;

@Builder
public record UsagePurposeResponse(
    Long id,
    String purpose,
    boolean reservationStatus
) {
    public static UsagePurposeResponse from(Purpose purpose) {
        return UsagePurposeResponse.builder()
            .id(purpose.getId())
            .purpose(purpose.getPurposeName())
            .reservationStatus(purpose.isReservationStatus())
            .build();
    }
}

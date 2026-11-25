package com.example.burserker.gujeuk.domain.user.presentation.dto.response;

import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationStatus;
import lombok.Builder;

@Builder
public record UsagePurposeResponse(
    Long id,
    String purpose,
    ReservationStatus reservationStatus
) {
    public static UsagePurposeResponse from(Purpose purpose) {
        return UsagePurposeResponse.builder()
            .id(purpose.getId())
            .purpose(purpose.getPurposeName())
            .reservationStatus(purpose.getReservationStatus())
            .build();
    }
}

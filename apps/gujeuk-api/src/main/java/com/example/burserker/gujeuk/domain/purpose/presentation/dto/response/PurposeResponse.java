package com.example.burserker.gujeuk.domain.purpose.presentation.dto.response;

import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationStatus;
import lombok.Builder;

@Builder
public record PurposeResponse(
        Long id,
        String purpose,
        ReservationStatus reservationStatus
) {
    public static PurposeResponse from(Purpose purpose) {
        return PurposeResponse.builder()
                .id(purpose.getId())
                .purpose(purpose.getPurposeName())
                .reservationStatus(purpose.getReservationStatus())
                .build();
    }
}

package com.example.burserker.gujeuk.domain.purpose.presentation.dto.response;

import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record PurposeWithSubResponse(
        Long purposeId,
        String purpose,
        ReservationStatus reservationStatus,
        List<SubPurposeResponse> subPurposes
) {
    public static PurposeWithSubResponse of(Purpose purpose, List<SubPurposeResponse> subPurposes) {
        return PurposeWithSubResponse.builder()
                .purposeId(purpose.getId())
                .purpose(purpose.getPurposeName())
                .reservationStatus(purpose.getReservationStatus())
                .subPurposes(subPurposes)
                .build();
    }
}

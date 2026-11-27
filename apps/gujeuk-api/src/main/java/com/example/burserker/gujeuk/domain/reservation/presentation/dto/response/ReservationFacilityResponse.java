package com.example.burserker.gujeuk.domain.reservation.presentation.dto.response;

import com.example.burserker.gujeuk.domain.purpose.domain.ReservationTarget;

public record ReservationFacilityResponse(
        Long purposeId,
        String purpose,
        Long subPurposeId,
        String subPurpose
) {
    public static ReservationFacilityResponse from(ReservationTarget target) {
        return new ReservationFacilityResponse(
                target.purpose().getId(),
                target.purpose().getPurposeName(),
                target.subPurpose() == null ? null : target.subPurpose().getId(),
                target.subPurpose() == null ? null : target.subPurpose().getSubPurposeName()
        );
    }
}

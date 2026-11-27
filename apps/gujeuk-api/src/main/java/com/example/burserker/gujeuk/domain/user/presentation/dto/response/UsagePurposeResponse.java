package com.example.burserker.gujeuk.domain.user.presentation.dto.response;

import com.example.burserker.gujeuk.domain.purpose.domain.Purpose;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationStatus;
import com.example.burserker.gujeuk.domain.purpose.presentation.dto.response.SubPurposeResponse;
import java.util.List;
import lombok.Builder;

@Builder
public record UsagePurposeResponse(
    Long id,
    String purpose,
    ReservationStatus reservationStatus,
    List<SubPurposeResponse> subPurposes
) {
    public static UsagePurposeResponse of(Purpose purpose, List<SubPurposeResponse> subPurposes) {
        return UsagePurposeResponse.builder()
            .id(purpose.getId())
            .purpose(purpose.getPurposeName())
            .reservationStatus(purpose.getReservationStatus())
            .subPurposes(subPurposes)
            .build();
    }
}

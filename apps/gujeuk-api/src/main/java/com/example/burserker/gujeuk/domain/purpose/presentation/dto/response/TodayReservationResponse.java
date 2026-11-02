package com.example.burserker.gujeuk.domain.purpose.presentation.dto.response;

import com.example.burserker.gujeuk.domain.purpose.domain.PurposeReservation;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record TodayReservationResponse(
        String purpose,
        @JsonFormat(pattern = "HH:mm:ss") LocalTime startTime,
        String name
) {
    public static TodayReservationResponse from(PurposeReservation reservation) {
        return new TodayReservationResponse(
                reservation.getPurpose().getPurposeName(),
                reservation.getStartTime().getTime(),
                reservation.getName()
        );
    }
}

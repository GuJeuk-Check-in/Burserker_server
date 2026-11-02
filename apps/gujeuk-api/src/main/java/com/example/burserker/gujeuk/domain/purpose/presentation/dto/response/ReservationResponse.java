package com.example.burserker.gujeuk.domain.purpose.presentation.dto.response;

import com.example.burserker.gujeuk.domain.purpose.domain.PurposeReservation;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record ReservationResponse (
    Long id,
    String purpose,
    String name,
    int maleCount,
    int femaleCount,
    String phone,

    @JsonFormat(pattern = "HH:mm:ss") LocalTime startTime,
    @JsonFormat(pattern = "HH:mm:ss") LocalTime endTime

){
    public static ReservationResponse from(PurposeReservation reservation){
        LocalTime startTime = reservation.getStartTime().getTime();

        return new ReservationResponse(
                reservation.getId(),
                reservation.getPurpose().getPurposeName(),
                reservation.getName(),
                reservation.getMaleCount(),
                reservation.getFemaleCount(),
                reservation.getRepresentativePhone(),
                startTime,
                startTime.plusHours(1)
        );
    }
}
